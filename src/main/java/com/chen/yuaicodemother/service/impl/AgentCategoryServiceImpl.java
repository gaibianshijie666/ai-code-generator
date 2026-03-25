package com.chen.yuaicodemother.service.impl;

import com.chen.yuaicodemother.mapper.AgentCategoryMapper;
import com.chen.yuaicodemother.model.entity.AgentCategory;
import com.chen.yuaicodemother.service.AgentCategoryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体分类 服务实现。
 *
 * @author chenchen
 */
@Service
@Slf4j
public class AgentCategoryServiceImpl extends ServiceImpl<AgentCategoryMapper, AgentCategory> implements AgentCategoryService {

    @Override
    public List<AgentCategory> listAllOrdered() {
        return list(QueryWrapper.create().orderBy("sortOrder", true));
    }
}
