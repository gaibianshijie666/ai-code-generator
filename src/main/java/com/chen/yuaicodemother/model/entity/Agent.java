package com.chen.yuaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;
import java.math.BigDecimal;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 实体类。
 *
 * @author chenchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("agent")
public class Agent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * Agent名称
     */
    private String name;

    /**
     * Agent描述
     */
    private String description;

    /**
     * Agent头像
     */
    private String avatar;

    /**
     * 系统提示词
     */
    @Column("systemPrompt")
    private String systemPrompt;

    /**
     * 启用的工具ID列表（JSON数组）
     */
    @Column("toolIds")
    private String toolIds;

    /**
     * 分类ID
     */
    @Column("categoryId")
    private Long categoryId;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 是否公开（0=私有 1=公开）
     */
    @Column("isPublic")
    private Integer isPublic;

    /**
     * 价格类型（free/points）
     */
    @Column("priceType")
    private String priceType;

    /**
     * 价格（积分）
     */
    private Integer price;

    /**
     * 创建用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 使用次数
     */
    @Column("useCount")
    private Integer useCount;

    /**
     * 克隆次数
     */
    @Column("cloneCount")
    private Integer cloneCount;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 状态（0=草稿 1=已发布 2=已下架）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
