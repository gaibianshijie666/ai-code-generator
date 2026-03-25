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
 * 用户积分 实体类。
 *
 * @author chenchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_points")
public class UserPoints implements Serializable {

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
     * 积分余额
     */
    private Integer balance;

    /**
     * 累计收入
     */
    @Column("totalIncome")
    private Integer totalIncome;

    /**
     * 累计支出
     */
    @Column("totalExpense")
    private Integer totalExpense;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

}
