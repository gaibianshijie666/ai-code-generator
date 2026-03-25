package com.chen.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chen.yuaicodemother.common.constant.UserConstant;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.exception.ThrowUtils;
import com.chen.yuaicodemother.mapper.AgentMapper;
import com.chen.yuaicodemother.model.dto.agent.AgentCreateRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentPublishRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentSquareQueryRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentUpdateRequest;
import com.chen.yuaicodemother.model.entity.Agent;
import com.chen.yuaicodemother.model.entity.AgentCategory;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.UserVO;
import com.chen.yuaicodemother.model.vo.agent.AgentSquareVO;
import com.chen.yuaicodemother.model.vo.agent.AgentVO;
import com.chen.yuaicodemother.service.AgentCategoryService;
import com.chen.yuaicodemother.service.AgentService;
import com.chen.yuaicodemother.service.UserService;
import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能体 服务层实现。
 *
 * @author chenchen
 */
@Service
@Slf4j
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

    @Resource
    private UserService userService;

    @Resource
    private AgentCategoryService agentCategoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAgent(AgentCreateRequest request, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getName()), ErrorCode.PARAMS_ERROR, "Agent 名称不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getSystemPrompt()), ErrorCode.PARAMS_ERROR, "系统 Prompt 不能为空");
        // 构造入库对象
        Agent agent = new Agent();
        BeanUtil.copyProperties(request, agent);
        // List 转 JSON 字符串存储
        if (request.getToolIds() != null) {
            agent.setToolIds(JSONUtil.toJsonStr(request.getToolIds()));
        }
        if (request.getTags() != null) {
            agent.setTags(JSONUtil.toJsonStr(request.getTags()));
        }
        agent.setUserId(loginUser.getId());
        agent.setStatus(0);
        agent.setIsPublic(0);
        agent.setUseCount(0);
        agent.setCloneCount(0);
        agent.setRating(BigDecimal.ZERO);
        agent.setCreateTime(LocalDateTime.now());
        agent.setUpdateTime(LocalDateTime.now());
        // 插入数据库
        boolean result = this.save(agent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 创建失败");
        log.info("Agent 创建成功，ID: {}, 名称: {}", agent.getId(), agent.getName());
        return agent.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgent(AgentUpdateRequest request, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 查询 Agent
        Agent agent = this.getById(request.getId());
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 权限校验，仅本人可更新
        if (!agent.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改该 Agent");
        }
        // 拷贝非空字段
        Agent updateAgent = new Agent();
        updateAgent.setId(request.getId());
        if (StrUtil.isNotBlank(request.getName())) {
            updateAgent.setName(request.getName());
        }
        if (request.getDescription() != null) {
            updateAgent.setDescription(request.getDescription());
        }
        if (request.getAvatar() != null) {
            updateAgent.setAvatar(request.getAvatar());
        }
        if (request.getSystemPrompt() != null) {
            updateAgent.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getToolIds() != null) {
            updateAgent.setToolIds(JSONUtil.toJsonStr(request.getToolIds()));
        }
        if (request.getCategoryId() != null) {
            updateAgent.setCategoryId(request.getCategoryId());
        }
        if (request.getTags() != null) {
            updateAgent.setTags(JSONUtil.toJsonStr(request.getTags()));
        }
        updateAgent.setUpdateTime(LocalDateTime.now());
        // 更新数据库
        boolean result = this.updateById(updateAgent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgent(Long id, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        // 查询 Agent
        Agent agent = this.getById(id);
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 权限校验，仅本人或管理员可删除
        if (!agent.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该 Agent");
        }
        // 删除
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 删除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAgent(AgentPublishRequest request, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 查询 Agent
        Agent agent = this.getById(request.getId());
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 权限校验，仅本人可发布
        if (!agent.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限发布该 Agent");
        }
        // 设置发布信息
        Agent updateAgent = new Agent();
        updateAgent.setId(request.getId());
        updateAgent.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : 1);
        updateAgent.setPriceType(StrUtil.isNotBlank(request.getPriceType()) ? request.getPriceType() : "free");
        updateAgent.setPrice(request.getPrice() != null ? request.getPrice() : 0);
        updateAgent.setStatus(1);
        updateAgent.setUpdateTime(LocalDateTime.now());
        // 更新数据库
        boolean result = this.updateById(updateAgent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 发布失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelfAgent(Long id, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        // 查询 Agent
        Agent agent = this.getById(id);
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 权限校验，仅本人可下架
        if (!agent.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下架该 Agent");
        }
        // 设置下架状态
        Agent updateAgent = new Agent();
        updateAgent.setId(id);
        updateAgent.setStatus(2);
        updateAgent.setUpdateTime(LocalDateTime.now());
        // 更新数据库
        boolean result = this.updateById(updateAgent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 下架失败");
    }

    @Override
    public Page<AgentSquareVO> getSquarePage(AgentSquareQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 构建查询条件：仅查询公开且已发布的 Agent
        QueryWrapper queryWrapper = getQueryWrapper(request);
        // 分页查询
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        Page<Agent> agentPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 转换为 AgentSquareVO 分页
        Page<AgentSquareVO> squareVOPage = new Page<>(pageNum, pageSize, agentPage.getTotalRow());
        List<AgentSquareVO> squareVOList = getAgentSquareVOList(agentPage.getRecords());
        squareVOPage.setRecords(squareVOList);
        return squareVOPage;
    }

    @Override
    public AgentSquareVO getSquareDetail(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        // 查询公开且已发布的 Agent
        Agent agent = this.getOne(QueryWrapper.create()
                .eq("id", id)
                .eq("isPublic", 1)
                .eq("status", 1));
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在或未发布");
        return getAgentSquareVO(agent);
    }

    @Override
    public AgentVO getAgentVO(Agent agent) {
        if (agent == null) {
            return null;
        }
        AgentVO agentVO = new AgentVO();
        BeanUtil.copyProperties(agent, agentVO);
        // 将 JSON 字符串转回 List
        if (StrUtil.isNotBlank(agent.getToolIds())) {
            agentVO.setToolIds(JSONUtil.toList(agent.getToolIds(), Long.class));
        }
        if (StrUtil.isNotBlank(agent.getTags())) {
            agentVO.setTags(JSONUtil.toList(agent.getTags(), String.class));
        }
        // 关联查询用户信息
        Long userId = agent.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            agentVO.setUser(userVO);
        }
        // 关联查询分类名称
        Long categoryId = agent.getCategoryId();
        if (categoryId != null) {
            AgentCategory category = agentCategoryService.getById(categoryId);
            if (category != null) {
                agentVO.setCategoryName(category.getName());
            }
        }
        return agentVO;
    }

    @Override
    public List<AgentVO> getAgentVOList(List<Agent> agents) {
        if (CollUtil.isEmpty(agents)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = agents.stream()
                .map(Agent::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 批量获取分类信息
        Set<Long> categoryIds = agents.stream()
                .map(Agent::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = CollUtil.isNotEmpty(categoryIds)
                ? agentCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(AgentCategory::getId, AgentCategory::getName))
                : Map.of();
        return agents.stream().map(agent -> {
            AgentVO agentVO = new AgentVO();
            BeanUtil.copyProperties(agent, agentVO);
            // JSON 字符串转 List
            if (StrUtil.isNotBlank(agent.getToolIds())) {
                agentVO.setToolIds(JSONUtil.toList(agent.getToolIds(), Long.class));
            }
            if (StrUtil.isNotBlank(agent.getTags())) {
                agentVO.setTags(JSONUtil.toList(agent.getTags(), String.class));
            }
            agentVO.setUser(userVOMap.get(agent.getUserId()));
            agentVO.setCategoryName(categoryNameMap.get(agent.getCategoryId()));
            return agentVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AgentSquareQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        // 仅查询公开且已发布的 Agent
        queryWrapper.eq("isPublic", 1).eq("status", 1);
        // 关键词搜索
        String keyword = request.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            QueryCondition keywordCondition = QueryCondition.create(new com.mybatisflex.core.query.QueryColumn("name"), SqlOperator.LIKE, "%" + keyword + "%")
                    .or(QueryCondition.create(new com.mybatisflex.core.query.QueryColumn("description"), SqlOperator.LIKE, "%" + keyword + "%"))
                    .or(QueryCondition.create(new com.mybatisflex.core.query.QueryColumn("tags"), SqlOperator.LIKE, "%" + keyword + "%"));
            queryWrapper.and(keywordCondition);
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
            queryWrapper.orderBy("rating", false);
        }
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cloneAgent(Long agentId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        // 查询源 Agent
        Agent sourceAgent = this.getById(agentId);
        ThrowUtils.throwIf(sourceAgent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 构造克隆对象
        Agent clonedAgent = new Agent();
        clonedAgent.setName(sourceAgent.getName() + "(克隆)");
        clonedAgent.setDescription(sourceAgent.getDescription());
        clonedAgent.setAvatar(sourceAgent.getAvatar());
        clonedAgent.setSystemPrompt(sourceAgent.getSystemPrompt());
        clonedAgent.setToolIds(sourceAgent.getToolIds());
        clonedAgent.setCategoryId(sourceAgent.getCategoryId());
        clonedAgent.setTags(sourceAgent.getTags());
        clonedAgent.setUserId(loginUser.getId());
        clonedAgent.setUseCount(0);
        clonedAgent.setCloneCount(0);
        clonedAgent.setRating(BigDecimal.ZERO);
        clonedAgent.setStatus(0);
        clonedAgent.setIsPublic(0);
        clonedAgent.setCreateTime(LocalDateTime.now());
        clonedAgent.setUpdateTime(LocalDateTime.now());
        // 保存克隆对象
        boolean result = this.save(clonedAgent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 克隆失败");
        // 增加源 Agent 克隆次数
        incrementCloneCount(agentId);
        log.info("Agent 克隆成功，源 ID: {}, 新 ID: {}", agentId, clonedAgent.getId());
        return clonedAgent.getId();
    }

    @Override
    public String exportAgent(Long agentId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR, "Agent ID 错误");
        // 查询 Agent
        Agent agent = this.getById(agentId);
        ThrowUtils.throwIf(agent == null, ErrorCode.NOT_FOUND_ERROR, "Agent 不存在");
        // 权限校验，仅本人可导出
        if (!agent.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限导出该 Agent");
        }
        // 构造导出数据
        JSONObject config = new JSONObject();
        config.set("name", agent.getName());
        config.set("description", agent.getDescription());
        config.set("systemPrompt", agent.getSystemPrompt());
        config.set("toolIds", agent.getToolIds());
        config.set("categoryId", agent.getCategoryId());
        config.set("tags", agent.getTags());
        return JSONUtil.toJsonStr(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importAgent(String configJson, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(configJson), ErrorCode.PARAMS_ERROR, "配置 JSON 不能为空");
        // 解析 JSON
        JSONObject config;
        try {
            config = JSONUtil.parseObj(configJson);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配置 JSON 格式错误");
        }
        String name = config.getStr("name");
        ThrowUtils.throwIf(StrUtil.isBlank(name), ErrorCode.PARAMS_ERROR, "Agent 名称不能为空");
        // 构造入库对象
        Agent agent = new Agent();
        agent.setName(name);
        agent.setDescription(config.getStr("description"));
        agent.setSystemPrompt(config.getStr("systemPrompt"));
        agent.setToolIds(config.getStr("toolIds"));
        agent.setCategoryId(config.getLong("categoryId"));
        agent.setTags(config.getStr("tags"));
        agent.setUserId(loginUser.getId());
        agent.setStatus(0);
        agent.setIsPublic(0);
        agent.setUseCount(0);
        agent.setCloneCount(0);
        agent.setRating(BigDecimal.ZERO);
        agent.setCreateTime(LocalDateTime.now());
        agent.setUpdateTime(LocalDateTime.now());
        // 保存
        boolean result = this.save(agent);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Agent 导入失败");
        log.info("Agent 导入成功，ID: {}, 名称: {}", agent.getId(), agent.getName());
        return agent.getId();
    }

    @Override
    public void incrementUseCount(Long agentId) {
        UpdateChain.of(Agent.class)
                .setRaw(Agent::getUseCount, "useCount + 1")
                .where("id = ?", agentId)
                .update();
    }

    @Override
    public void incrementCloneCount(Long agentId) {
        UpdateChain.of(Agent.class)
                .setRaw(Agent::getCloneCount, "cloneCount + 1")
                .where("id = ?", agentId)
                .update();
    }

    // ==================== 内部方法 ====================

    /**
     * 获取 Agent 广场 VO（单个）
     *
     * @param agent Agent 实体
     * @return Agent 广场 VO
     */
    private AgentSquareVO getAgentSquareVO(Agent agent) {
        if (agent == null) {
            return null;
        }
        AgentSquareVO squareVO = new AgentSquareVO();
        BeanUtil.copyProperties(agent, squareVO);
        // 将 JSON 字符串转回 List
        if (StrUtil.isNotBlank(agent.getTags())) {
            squareVO.setTags(JSONUtil.toList(agent.getTags(), String.class));
        }
        // 关联查询用户信息
        Long userId = agent.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            squareVO.setUser(userVO);
        }
        // 关联查询分类名称
        Long categoryId = agent.getCategoryId();
        if (categoryId != null) {
            AgentCategory category = agentCategoryService.getById(categoryId);
            if (category != null) {
                squareVO.setCategoryName(category.getName());
            }
        }
        return squareVO;
    }

    /**
     * 批量获取 Agent 广场 VO 列表（带 N+1 优化）
     *
     * @param agents Agent 实体列表
     * @return Agent 广场 VO 列表
     */
    private List<AgentSquareVO> getAgentSquareVOList(List<Agent> agents) {
        if (CollUtil.isEmpty(agents)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = agents.stream()
                .map(Agent::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 批量获取分类信息
        Set<Long> categoryIds = agents.stream()
                .map(Agent::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = CollUtil.isNotEmpty(categoryIds)
                ? agentCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(AgentCategory::getId, AgentCategory::getName))
                : Map.of();
        return agents.stream().map(agent -> {
            AgentSquareVO squareVO = new AgentSquareVO();
            BeanUtil.copyProperties(agent, squareVO);
            // JSON 字符串转 List
            if (StrUtil.isNotBlank(agent.getTags())) {
                squareVO.setTags(JSONUtil.toList(agent.getTags(), String.class));
            }
            squareVO.setUser(userVOMap.get(agent.getUserId()));
            squareVO.setCategoryName(categoryNameMap.get(agent.getCategoryId()));
            return squareVO;
        }).collect(Collectors.toList());
    }
}
