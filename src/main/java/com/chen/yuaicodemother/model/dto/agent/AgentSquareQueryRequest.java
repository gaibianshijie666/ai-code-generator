package com.chen.yuaicodemother.model.dto.agent;

import com.chen.yuaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AgentSquareQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 排序字段（默认按评分排序）
     */
    private String sortField = "rating";

    /**
     * 排序顺序
     */
    private String sortOrder = "descend";

    private static final long serialVersionUID = 1L;
}
