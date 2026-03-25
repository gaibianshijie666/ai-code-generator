package com.chen.yuaicodemother.model.vo.agent;

import com.chen.yuaicodemother.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentVO implements Serializable {

    /**
     * Agent ID
     */
    private Long id;

    /**
     * Agent 名称
     */
    private String name;

    /**
     * Agent 描述
     */
    private String description;

    /**
     * Agent 头像
     */
    private String avatar;

    /**
     * 系统 Prompt
     */
    private String systemPrompt;

    /**
     * 关联工具 ID 列表
     */
    private List<Long> toolIds;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 是否公开（0-私有，1-公开）
     */
    private Integer isPublic;

    /**
     * 价格类型
     */
    private String priceType;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 创建用户 ID
     */
    private Long userId;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 克隆次数
     */
    private Integer cloneCount;

    /**
     * 平均评分
     */
    private BigDecimal rating;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;
}
