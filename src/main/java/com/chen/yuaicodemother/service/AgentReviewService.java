package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.entity.AgentReview;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 智能体评价 服务层。
 *
 * @author chenchen
 */
public interface AgentReviewService extends IService<AgentReview> {

    /**
     * 提交评价
     *
     * @param agentId 智能体ID
     * @param rating  评分
     * @param content 评价内容
     * @param userId  用户ID
     */
    void submitReview(Long agentId, Integer rating, String content, Long userId);

    /**
     * 分页获取评价列表
     *
     * @param agentId  智能体ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页评价列表
     */
    Page<AgentReview> getReviewPage(Long agentId, int pageNum, int pageSize);
}
