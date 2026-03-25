package com.chen.yuaicodemother.model.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AgentImportRequest implements Serializable {

    /**
     * 导入的 Agent 配置 JSON
     */
    @NotBlank(message = "Agent 配置 JSON 不能为空")
    private String configJson;

    private static final long serialVersionUID = 1L;
}
