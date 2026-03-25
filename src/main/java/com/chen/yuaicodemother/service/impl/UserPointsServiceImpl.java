package com.chen.yuaicodemother.service.impl;

import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.mapper.UserPointsLogMapper;
import com.chen.yuaicodemother.mapper.UserPointsMapper;
import com.chen.yuaicodemother.model.entity.UserPoints;
import com.chen.yuaicodemother.model.entity.UserPointsLog;
import com.chen.yuaicodemother.model.enums.PointsTypeEnum;
import com.chen.yuaicodemother.service.UserPointsService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户积分 服务实现。
 *
 * @author chenchen
 */
@Service
@Slf4j
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {

    @Resource
    private UserPointsLogMapper userPointsLogMapper;

    @Override
    public int getBalance(Long userId) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        return userPoints.getBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, int amount, String description) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setBalance(userPoints.getBalance() + amount);
        userPoints.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(userPoints);
        if (!updated) {
            return false;
        }
        savePointsLog(userId, amount, userPoints.getBalance(), PointsTypeEnum.RECHARGE.getValue(), description, null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consume(Long userId, int amount, Long agentId, String description) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        if (userPoints.getBalance() < amount) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
        }
        userPoints.setBalance(userPoints.getBalance() - amount);
        userPoints.setTotalExpense(userPoints.getTotalExpense() + amount);
        userPoints.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(userPoints);
        if (!updated) {
            return false;
        }
        savePointsLog(userId, -amount, userPoints.getBalance(), PointsTypeEnum.CONSUME.getValue(), description, agentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addIncome(Long userId, int amount, Long agentId, String description) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setBalance(userPoints.getBalance() + amount);
        userPoints.setTotalIncome(userPoints.getTotalIncome() + amount);
        userPoints.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(userPoints);
        if (!updated) {
            return false;
        }
        savePointsLog(userId, amount, userPoints.getBalance(), PointsTypeEnum.INCOME.getValue(), description, agentId);
        return true;
    }

    @Override
    public Page<UserPointsLog> getPointsLogPage(Long userId, int pageNum, int pageSize) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .orderBy("createTime", false);
        return userPointsLogMapper.paginate(Page.of(pageNum, pageSize), queryWrapper);
    }

    /**
     * 获取或创建用户积分记录
     *
     * @param userId 用户ID
     * @return 用户积分记录
     */
    private UserPoints getOrCreateUserPoints(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create().eq("userId", userId);
        UserPoints userPoints = this.getOne(queryWrapper);
        if (userPoints == null) {
            userPoints = UserPoints.builder()
                    .userId(userId)
                    .balance(0)
                    .totalIncome(0)
                    .totalExpense(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            this.save(userPoints);
        }
        return userPoints;
    }

    /**
     * 保存积分变动日志
     *
     * @param userId        用户ID
     * @param amount        变动金额
     * @param balanceAfter  变动后余额
     * @param type          类型
     * @param description   描述
     * @param agentId       关联Agent ID
     */
    private void savePointsLog(Long userId, int amount, int balanceAfter, String type, String description, Long agentId) {
        UserPointsLog log = UserPointsLog.builder()
                .userId(userId)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .type(type)
                .description(description)
                .agentId(agentId)
                .createTime(LocalDateTime.now())
                .build();
        userPointsLogMapper.insert(log);
    }
}
