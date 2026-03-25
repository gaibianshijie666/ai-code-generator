package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.entity.AgentUsage;
import com.mybatisflex.core.service.IService;

/**
 * 智能体使用记录 服务层。
 *
 * @author chenchen
 */
public interface AgentUsageService extends IService<AgentUsage> {

    /**
     * 检查用户是否已有免费试用记录
     *
     * @param agentId 智能体 ID
     * @param userId  用户 ID
     * @return true 表示已有记录（已使用过），false 表示未使用过
     */
    boolean hasFreeTrial(Long agentId, Long userId);

    /**
     * 记录智能体使用记录
     *
     * @param agentId    智能体 ID
     * @param userId     用户 ID
     * @param isFree     是否免费试用
     * @param pointsCost 消耗积分
     */
    void recordUsage(Long agentId, Long userId, boolean isFree, int pointsCost);
}
