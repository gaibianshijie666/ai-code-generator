package com.chen.yuaicodemother.model.dto.points;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class PointsRechargeRequest implements Serializable {

    /**
     * 充值积分数量
     */
    @NotNull(message = "充值积分数量不能为空")
    @Min(value = 1, message = "充值积分数量最少为 1")
    private Integer amount;

    private static final long serialVersionUID = 1L;
}
