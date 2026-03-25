package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.dto.agent.AgentCreateRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentPublishRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentSquareQueryRequest;
import com.chen.yuaicodemother.model.dto.agent.AgentUpdateRequest;
import com.chen.yuaicodemother.model.entity.Agent;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.agent.AgentSquareVO;
import com.chen.yuaicodemother.model.vo.agent.AgentVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * 智能体 服务层。
 *
 * @author chenchen
 */
public interface AgentService extends IService<Agent> {

    /**
     * 创建 Agent
     *
     * @param request   创建请求
     * @param loginUser 当前登录用户
     * @return 新创建的 Agent ID
     */
    Long createAgent(AgentCreateRequest request, User loginUser);

    /**
     * 更新 Agent
     *
     * @param request   更新请求
     * @param loginUser 当前登录用户
     */
    void updateAgent(AgentUpdateRequest request, User loginUser);

    /**
     * 删除 Agent
     *
     * @param id        Agent ID
     * @param loginUser 当前登录用户
     */
    void deleteAgent(Long id, User loginUser);

    /**
     * 发布 Agent
     *
     * @param request   发布请求
     * @param loginUser 当前登录用户
     */
    void publishAgent(AgentPublishRequest request, User loginUser);

    /**
     * 下架 Agent
     *
     * @param id        Agent ID
     * @param loginUser 当前登录用户
     */
    void offShelfAgent(Long id, User loginUser);

    /**
     * 分页查询 Agent 广场（公开已发布的 Agent）
     *
     * @param request 分页查询请求
     * @return Agent 广场分页数据
     */
    Page<AgentSquareVO> getSquarePage(AgentSquareQueryRequest request);

    /**
     * 获取 Agent 广场详情
     *
     * @param id Agent ID
     * @return Agent 广场 VO
     */
    AgentSquareVO getSquareDetail(Long id);

    /**
     * 获取 Agent VO
     *
     * @param agent Agent 实体
     * @return Agent VO
     */
    AgentVO getAgentVO(Agent agent);

    /**
     * 批量获取 Agent VO 列表
     *
     * @param agents Agent 实体列表
     * @return Agent VO 列表
     */
    List<AgentVO> getAgentVOList(List<Agent> agents);

    /**
     * 构造 Agent 广场查询条件
     *
     * @param request 广场查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AgentSquareQueryRequest request);

    /**
     * 克隆 Agent
     *
     * @param agentId   源 Agent ID
     * @param loginUser 当前登录用户
     * @return 新 Agent 的 ID
     */
    Long cloneAgent(Long agentId, User loginUser);

    /**
     * 导出 Agent 配置
     *
     * @param agentId   Agent ID
     * @param loginUser 当前登录用户
     * @return Agent 配置 JSON 字符串
     */
    String exportAgent(Long agentId, User loginUser);

    /**
     * 导入 Agent 配置
     *
     * @param configJson Agent 配置 JSON 字符串
     * @param loginUser  当前登录用户
     * @return 新 Agent 的 ID
     */
    Long importAgent(String configJson, User loginUser);

    /**
     * 增加使用次数（原子操作）
     *
     * @param agentId Agent ID
     */
    void incrementUseCount(Long agentId);

    /**
     * 增加克隆次数（原子操作）
     *
     * @param agentId Agent ID
     */
    void incrementCloneCount(Long agentId);
}
