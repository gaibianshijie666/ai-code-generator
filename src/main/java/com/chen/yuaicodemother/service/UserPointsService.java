package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.entity.UserPoints;
import com.chen.yuaicodemother.model.entity.UserPointsLog;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 用户积分 服务层。
 *
 * @author chenchen
 */
public interface UserPointsService extends IService<UserPoints> {

    /**
     * 获取用户积分余额
     *
     * @param userId 用户ID
     * @return 积分余额
     */
    int getBalance(Long userId);

    /**
     * 充值积分
     *
     * @param userId      用户ID
     * @param amount      充值金额
     * @param description 描述
     * @return 是否成功
     */
    boolean recharge(Long userId, int amount, String description);

    /**
     * 消费积分
     *
     * @param userId      用户ID
     * @param amount      消费金额
     * @param agentId     关联Agent ID
     * @param description 描述
     * @return 是否成功
     */
    boolean consume(Long userId, int amount, Long agentId, String description);

    /**
     * 增加收入积分
     *
     * @param userId      用户ID
     * @param amount      金额
     * @param agentId     关联Agent ID
     * @param description 描述
     * @return 是否成功
     */
    boolean addIncome(Long userId, int amount, Long agentId, String description);

    /**
     * 分页查询用户积分日志
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 积分日志分页数据
     */
    Page<UserPointsLog> getPointsLogPage(Long userId, int pageNum, int pageSize);
}
