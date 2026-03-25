package com.chen.yuaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分日志 实体类。
 *
 * @author chenchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_points_log")
public class UserPointsLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 变动金额（正=收入 负=支出）
     */
    private Integer amount;

    /**
     * 变动后余额
     */
    @Column("balanceAfter")
    private Integer balanceAfter;

    /**
     * 类型（recharge/consume/income）
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联Agent ID
     */
    @Column("agentId")
    private Long agentId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

}
