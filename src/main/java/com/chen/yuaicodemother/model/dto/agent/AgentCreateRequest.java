package com.chen.yuaicodemother.model.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AgentCreateRequest implements Serializable {

    /**
     * Agent 名称
     */
    @NotBlank(message = "Agent 名称不能为空")
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
    @NotBlank(message = "系统 Prompt 不能为空")
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
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
