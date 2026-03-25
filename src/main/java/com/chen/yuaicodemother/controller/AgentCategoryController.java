package com.chen.yuaicodemother.controller;

import cn.hutool.core.util.StrUtil;
import com.chen.yuaicodemother.annotation.AuthCheck;
import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.common.ResultUtils;
import com.chen.yuaicodemother.common.constant.UserConstant;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.exception.ThrowUtils;
import com.chen.yuaicodemother.model.entity.AgentCategory;
import com.chen.yuaicodemother.service.AgentCategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体分类 控制层。
 *
 * @author chenchen
 */
@RestController
@RequestMapping("/agent/category")
public class AgentCategoryController {

    @Resource
    private AgentCategoryService agentCategoryService;

    /**
     * 获取所有分类（按 sortOrder 排序）
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    public BaseResponse<List<AgentCategory>> listAll() {
        return ResultUtils.success(agentCategoryService.listAllOrdered());
    }

    /**
     * 管理员创建分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    @PostMapping("/admin/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> createCategory(@RequestBody AgentCategory category) {
        ThrowUtils.throwIf(category == null || StrUtil.isBlank(category.getName()),
                ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        boolean result = agentCategoryService.save(category);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCategory(@RequestBody AgentCategory category) {
        ThrowUtils.throwIf(category == null || category.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = agentCategoryService.updateById(category);
        return ResultUtils.success(result);
    }
}
