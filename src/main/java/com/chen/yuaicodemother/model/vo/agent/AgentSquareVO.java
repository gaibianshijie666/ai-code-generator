package com.chen.yuaicodemother.model.vo.agent;

import com.chen.yuaicodemother.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentSquareVO implements Serializable {

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
     * 价格类型
     */
    private String priceType;

    /**
     * 价格
     */
    private Integer price;

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
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
