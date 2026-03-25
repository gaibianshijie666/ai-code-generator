package com.chen.yuaicodemother.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chen.yuaicodemother.annotation.AuthCheck;
import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.common.DeleteRequest;
import com.chen.yuaicodemother.common.PageRequest;
import com.chen.yuaicodemother.common.ResultUtils;
import com.chen.yuaicodemother.common.constant.UserConstant;
import com.chen.yuaicodemother.core.AgentChatFacade;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.exception.ThrowUtils;
import com.chen.yuaicodemother.model.dto.agent.*;
import com.chen.yuaicodemother.model.entity.Agent;
import com.chen.yuaicodemother.model.entity.AgentReview;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.agent.AgentSquareVO;
import com.chen.yuaicodemother.model.vo.agent.AgentVO;
import com.chen.yuaicodemother.service.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 智能体 控制层。
 *
 * @author chenchen
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    @Resource
    private AgentReviewService agentReviewService;

    @Resource
    private AgentUsageService agentUsageService;

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private UserService userService;

    @Resource
    private AgentChatFacade agentChatFacade;

    // ==================== Agent CRUD ====================

    /**
     * 创建 Agent
     *
     * @param request    创建请求
     * @param httpRequest HTTP 请求
     * @return Agent ID
     */
    @PostMapping("/create")
    public BaseResponse<Long> createAgent(@Valid @RequestBody AgentCreateRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long agentId = agentService.createAgent(request, loginUser);
        return ResultUtils.success(agentId);
    }

    /**
     * 更新 Agent
     *
     * @param request    更新请求
     * @param httpRequest HTTP 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateAgent(@RequestBody AgentUpdateRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        agentService.updateAgent(request, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 删除 Agent
     *
     * @param request    删除请求
     * @param httpRequest HTTP 请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        agentService.deleteAgent(request.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取 Agent VO 详情
     *
     * @param id Agent ID
     * @return Agent VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<AgentVO> getAgentVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Agent agent = agentService.getById(id);
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        return ResultUtils.success(agentService.getAgentVO(agent));
    }

    /**
     * 分页获取当前用户创建的 Agent 列表
     *
     * @param request    查询请求
     * @param httpRequest HTTP 请求
     * @return Agent 列表
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<AgentVO>> listMyAgentByPage(@RequestBody AgentSquareQueryRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        // 构建查询条件：查询当前用户的所有 Agent（不限公开/状态）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", loginUser.getId());
        // 关键词搜索
        String keyword = request.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and("name LIKE ? OR description LIKE ? OR tags LIKE ?",
                    "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%");
        }
        // 分类筛选
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            queryWrapper.eq("categoryId", categoryId);
        }
        // 排序
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAscend = "ascend".equals(sortOrder);
            queryWrapper.orderBy(sortField, isAscend);
        } else {
            queryWrapper.orderBy("updateTime", false);
        }
        // 分页查询
        Page<Agent> agentPage = agentService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AgentVO> agentVOPage = new Page<>(pageNum, pageSize, agentPage.getTotalRow());
        List<AgentVO> agentVOList = agentService.getAgentVOList(agentPage.getRecords());
        agentVOPage.setRecords(agentVOList);
        return ResultUtils.success(agentVOPage);
    }

    // ==================== Publish ====================

    /**
     * 发布 Agent
     *
     * @param request    发布请求
     * @param httpRequest HTTP 请求
     * @return 发布结果
     */
    @PostMapping("/publish")
    public BaseResponse<Boolean> publishAgent(@RequestBody AgentPublishRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        agentService.publishAgent(request, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 下架 Agent
     *
     * @param request    删除请求（复用 id 字段）
     * @param httpRequest HTTP 请求
     * @return 下架结果
     */
    @PostMapping("/offShelf")
    public BaseResponse<Boolean> offShelfAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        agentService.offShelfAgent(request.getId(), loginUser);
        return ResultUtils.success(true);
    }

    // ==================== Square (public) ====================

    /**
     * 分页获取 Agent 广场列表
     *
     * @param request 查询请求
     * @return Agent 广场列表
     */
    @PostMapping("/square/list/page")
    public BaseResponse<Page<AgentSquareVO>> listSquareByPage(@RequestBody AgentSquareQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Page<AgentSquareVO> squarePage = agentService.getSquarePage(request);
        return ResultUtils.success(squarePage);
    }

    /**
     * 获取 Agent 广场详情
     *
     * @param id Agent ID
     * @return Agent 广场 VO
     */
    @GetMapping("/square/detail/{id}")
    public BaseResponse<AgentSquareVO> getSquareDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AgentSquareVO squareVO = agentService.getSquareDetail(id);
        return ResultUtils.success(squareVO);
    }

    // ==================== Chat (SSE) ====================

    /**
     * 与 Agent 对话（SSE 流式返回）
     *
     * @param agentId    Agent ID
     * @param message    用户消息
     * @param httpRequest HTTP 请求
     * @return SSE 流式响应
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatWithAgent(@RequestParam Long agentId,
                                                       @RequestParam String message,
                                                       HttpServletRequest httpRequest) {
        // 参数校验
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        // 获取登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        // 查询 Agent
        Agent agent = agentService.getById(agentId);
        ThrowUtils.throwIf(agent == null || agent.getIsPublic() != 1 || agent.getStatus() != 1,
                ErrorCode.NOT_FOUND_ERROR, "智能体不存在或未发布");
        // 免费试用与积分扣费逻辑
        boolean hasFreeTrial = agentUsageService.hasFreeTrial(agentId, loginUser.getId());
        if (!hasFreeTrial && "points".equals(agent.getPriceType()) && agent.getPrice() != null && agent.getPrice() > 0) {
            int price = agent.getPrice();
            // 扣除用户积分
            userPointsService.consume(loginUser.getId(), price, agentId, "使用智能体: " + agent.getName());
            // 给创作者增加收入（80%）
            int creatorIncome = (int) (price * 0.8);
            userPointsService.addIncome(agent.getUserId(), creatorIncome, agentId, "智能体收入: " + agent.getName());
            // 记录使用
            agentUsageService.recordUsage(agentId, loginUser.getId(), false, price);
        } else {
            // 记录使用
            agentUsageService.recordUsage(agentId, loginUser.getId(), hasFreeTrial, 0);
        }
        // 增加使用次数
        agentService.incrementUseCount(agentId);
        // 调用 Agent 对话门面
        Flux<String> contentFlux = agentChatFacade.chat(agent, loginUser.getId(), message);
        // 封装 SSE 响应（与 AppController 保持一致）
        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    // ==================== Clone & Export/Import ====================

    /**
     * 克隆 Agent
     *
     * @param request    删除请求（复用 id 字段）
     * @param httpRequest HTTP 请求
     * @return 新 Agent ID
     */
    @PostMapping("/clone")
    public BaseResponse<Long> cloneAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long newAgentId = agentService.cloneAgent(request.getId(), loginUser);
        return ResultUtils.success(newAgentId);
    }

    /**
     * 导出 Agent 配置
     *
     * @param id          Agent ID
     * @param httpRequest HTTP 请求
     * @return Agent 配置 JSON
     */
    @GetMapping("/export/{id}")
    public BaseResponse<String> exportAgent(@PathVariable Long id, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        String configJson = agentService.exportAgent(id, loginUser);
        return ResultUtils.success(configJson);
    }

    /**
     * 导入 Agent 配置
     *
     * @param request    导入请求
     * @param httpRequest HTTP 请求
     * @return 新 Agent ID
     */
    @PostMapping("/import")
    public BaseResponse<Long> importAgent(@RequestBody AgentImportRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || StrUtil.isBlank(request.getConfigJson()), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long agentId = agentService.importAgent(request.getConfigJson(), loginUser);
        return ResultUtils.success(agentId);
    }

    // ==================== Review ====================

    /**
     * 提交评价
     *
     * @param request    评价提交请求
     * @param httpRequest HTTP 请求
     * @return 提交结果
     */
    @PostMapping("/review/submit")
    public BaseResponse<Boolean> submitReview(@Valid @RequestBody AgentReviewSubmitRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        agentReviewService.submitReview(request.getAgentId(), request.getRating(), request.getContent(), loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 分页获取评价列表
     *
     * @param request  分页请求
     * @param agentId  Agent ID
     * @return 评价列表
     */
    @PostMapping("/review/list/page")
    public BaseResponse<Page<AgentReview>> listReviewByPage(@RequestBody PageRequest request, @RequestParam Long agentId) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        Page<AgentReview> reviewPage = agentReviewService.getReviewPage(agentId, request.getPageNum(), request.getPageSize());
        return ResultUtils.success(reviewPage);
    }

    // ==================== Admin ====================

    /**
     * 管理员更新 Agent
     *
     * @param request 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAgentByAdmin(@RequestBody AgentPublishRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        Agent agent = agentService.getById(request.getId());
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 管理员更新发布信息
        Agent updateAgent = new Agent();
        updateAgent.setId(request.getId());
        if (request.getIsPublic() != null) {
            updateAgent.setIsPublic(request.getIsPublic());
        }
        if (StrUtil.isNotBlank(request.getPriceType())) {
            updateAgent.setPriceType(request.getPriceType());
        }
        if (request.getPrice() != null) {
            updateAgent.setPrice(request.getPrice());
        }
        updateAgent.setUpdateTime(java.time.LocalDateTime.now());
        boolean result = agentService.updateById(updateAgent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "管理员更新 Agent 失败");
        return ResultUtils.success(true);
    }

    /**
     * 管理员删除 Agent
     *
     * @param request 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAgentByAdmin(@RequestBody DeleteRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Agent agent = agentService.getById(request.getId());
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        boolean result = agentService.removeById(request.getId());
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页获取 Agent 列表
     *
     * @param request 查询请求
     * @return Agent 列表
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AgentVO>> listAgentByAdmin(@RequestBody AgentSquareQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        // 管理员查询所有 Agent（不限公开/状态），使用 getQueryWrapper 但去掉公开/状态限制
        QueryWrapper queryWrapper = QueryWrapper.create();
        // 关键词搜索
        String keyword = request.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and("name LIKE ? OR description LIKE ? OR tags LIKE ?",
                    "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%");
        }
        // 分类筛选
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            queryWrapper.eq("categoryId", categoryId);
        }
        // 排序
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAscend = "ascend".equals(sortOrder);
            queryWrapper.orderBy(sortField, isAscend);
        } else {
            queryWrapper.orderBy("updateTime", false);
        }
        // 分页查询
        Page<Agent> agentPage = agentService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AgentVO> agentVOPage = new Page<>(pageNum, pageSize, agentPage.getTotalRow());
        List<AgentVO> agentVOList = agentService.getAgentVOList(agentPage.getRecords());
        agentVOPage.setRecords(agentVOList);
        return ResultUtils.success(agentVOPage);
    }
}
