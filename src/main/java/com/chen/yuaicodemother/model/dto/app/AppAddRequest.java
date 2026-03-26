package com.chen.yuaicodemother.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    @NotBlank(message = "初始化 prompt 不能为空")
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
