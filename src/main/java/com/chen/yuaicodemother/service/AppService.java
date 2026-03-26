package com.chen.yuaicodemother.service;

import com.chen.yuaicodemother.model.dto.app.AppAddRequest;
import com.chen.yuaicodemother.model.dto.app.AppQueryRequest;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.chen.yuaicodemother.model.entity.App;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author chenchen
 */
public interface AppService extends IService<App> {

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 ID
     * @param message   提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 应用部署
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}