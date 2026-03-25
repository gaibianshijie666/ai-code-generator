package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.entity.AgentCategory;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 智能体分类 服务层。
 *
 * @author chenchen
 */
public interface AgentCategoryService extends IService<AgentCategory> {

    /**
     * 获取所有分类，按 sortOrder 排序
     *
     * @return 排序后的分类列表
     */
    List<AgentCategory> listAllOrdered();
}
