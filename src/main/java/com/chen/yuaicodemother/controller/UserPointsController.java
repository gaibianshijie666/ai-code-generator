package com.chen.yuaicodemother.controller;

import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.common.PageRequest;
import com.chen.yuaicodemother.common.ResultUtils;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.exception.ThrowUtils;
import com.chen.yuaicodemother.model.dto.points.PointsRechargeRequest;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.entity.UserPointsLog;
import com.chen.yuaicodemother.service.UserPointsService;
import com.chen.yuaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户积分 控制层。
 *
 * @author chenchen
 */
@RestController
@RequestMapping("/points")
public class UserPointsController {

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户积分余额
     *
     * @param request HTTP 请求
     * @return 积分余额
     */
    @GetMapping("/balance")
    public BaseResponse<Integer> getBalance(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userPointsService.getBalance(loginUser.getId()));
    }

    /**
     * 手动充值积分
     *
     * @param pointsRechargeRequest 充值请求
     * @param httpRequest           HTTP 请求
     * @return 是否成功
     */
    @PostMapping("/recharge")
    public BaseResponse<Boolean> recharge(@Valid @RequestBody PointsRechargeRequest pointsRechargeRequest,
                                          HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(pointsRechargeRequest.getAmount() == null || pointsRechargeRequest.getAmount() <= 0,
                ErrorCode.PARAMS_ERROR, "充值金额无效");
        userPointsService.recharge(loginUser.getId(), pointsRechargeRequest.getAmount(), "手动充值");
        return ResultUtils.success(true);
    }

    /**
     * 分页查询当前用户积分日志
     *
     * @param pageRequest 分页请求
     * @param httpRequest HTTP 请求
     * @return 积分日志分页数据
     */
    @PostMapping("/log/list/page")
    public BaseResponse<Page<UserPointsLog>> listLogByPage(@RequestBody PageRequest pageRequest,
                                                           HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<UserPointsLog> page = userPointsService.getPointsLogPage(
                loginUser.getId(), pageRequest.getPageNum(), pageRequest.getPageSize());
        return ResultUtils.success(page);
    }
}
