package com.chen.yuaicodemother.service.impl;

import com.chen.yuaicodemother.mapper.AgentUsageMapper;
import com.chen.yuaicodemother.model.entity.AgentUsage;
import com.chen.yuaicodemother.service.AgentUsageService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 智能体使用记录 服务实现。
 *
 * @author chenchen
 */
@Service
@Slf4j
public class AgentUsageServiceImpl extends ServiceImpl<AgentUsageMapper, AgentUsage> implements AgentUsageService {

    @Override
    public boolean hasFreeTrial(Long agentId, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agentId", agentId)
                .eq("userId", userId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public void recordUsage(Long agentId, Long userId, boolean isFree, int pointsCost) {
        AgentUsage agentUsage = AgentUsage.builder()
                .agentId(agentId)
                .userId(userId)
                .isFree(isFree ? 1 : 0)
                .pointsCost(pointsCost)
                .createTime(LocalDateTime.now())
                .build();
        this.save(agentUsage);
        log.info("记录智能体使用记录，agentId: {}, userId: {}, isFree: {}, pointsCost: {}", agentId, userId, isFree, pointsCost);
    }
}
