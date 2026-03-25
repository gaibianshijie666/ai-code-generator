package com.chen.yuaicodemother.model.dto.agent;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AgentReviewSubmitRequest implements Serializable {

    /**
     * Agent ID
     */
    @NotNull(message = "Agent ID 不能为空")
    private Long agentId;

    /**
     * 评分（1-5）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为 1")
    @Max(value = 5, message = "评分最高为 5")
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
