package com.chen.yuaicodemother.service.impl;

import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.mapper.AgentMapper;
import com.chen.yuaicodemother.mapper.AgentReviewMapper;
import com.chen.yuaicodemother.model.entity.Agent;
import com.chen.yuaicodemother.model.entity.AgentReview;
import com.chen.yuaicodemother.service.AgentReviewService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体评价 服务实现。
 *
 * @author chenchen
 */
@Service
@Slf4j
public class AgentReviewServiceImpl extends ServiceImpl<AgentReviewMapper, AgentReview> implements AgentReviewService {

    @Resource
    private AgentMapper agentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long agentId, Integer rating, String content, Long userId) {
        // 检查是否已评价
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agentId", agentId)
                .eq("userId", userId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已评价过该智能体");
        }

        // 保存评价
        AgentReview review = AgentReview.builder()
                .agentId(agentId)
                .userId(userId)
                .rating(rating)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
        this.save(review);

        // 重新计算平均评分
        QueryWrapper reviewQueryWrapper = QueryWrapper.create()
                .eq("agentId", agentId);
        List<AgentReview> reviews = this.list(reviewQueryWrapper);

        BigDecimal avgRating = BigDecimal.ZERO;
        if (!reviews.isEmpty()) {
            int totalRating = reviews.stream()
                    .mapToInt(AgentReview::getRating)
                    .sum();
            avgRating = BigDecimal.valueOf(totalRating)
                    .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
        }

        // 更新智能体评分
        Agent agent = agentMapper.selectOneById(agentId);
        if (agent != null) {
            agent.setRating(avgRating);
            agentMapper.update(agent);
        }

        log.info("用户提交评价，agentId: {}, userId: {}, rating: {}", agentId, userId, rating);
    }

    @Override
    public Page<AgentReview> getReviewPage(Long agentId, int pageNum, int pageSize) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agentId", agentId)
                .orderBy("createTime", false);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }
}
