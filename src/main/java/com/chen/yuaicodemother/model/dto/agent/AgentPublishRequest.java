package com.chen.yuaicodemother.model.dto.agent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AgentPublishRequest implements Serializable {

    /**
     * Agent ID
     */
    @NotNull(message = "Agent ID 不能为空")
    private Long id;

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

    private static final long serialVersionUID = 1L;
}
