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
 * Agent使用记录 实体类。
 *
 * @author chenchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("agent_usage")
public class AgentUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * Agent ID
     */
    @Column("agentId")
    private Long agentId;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 是否免费试用（0=付费 1=免费试用）
     */
    @Column("isFree")
    private Integer isFree;

    /**
     * 消耗积分
     */
    @Column("pointsCost")
    private Integer pointsCost;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

}
