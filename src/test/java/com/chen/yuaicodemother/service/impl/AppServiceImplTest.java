package com.chen.yuaicodemother.service.impl;

import cn.hutool.core.util.IdUtil;
import com.chen.yuaicodemother.model.dto.app.AppAddRequest;
import com.chen.yuaicodemother.model.dto.app.AppQueryRequest;
import com.chen.yuaicodemother.model.entity.App;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.chen.yuaicodemother.model.vo.AppVO;
import com.chen.yuaicodemother.service.AppService;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppServiceImplTest {

    @Mock
    private AppService appService;

    @InjectMocks
    private AppServiceImpl appServiceImpl;

    private User testUser;
    private App testApp;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testUser");

        testApp = new App();
        testApp.setId(1L);
        testApp.setAppName("测试应用");
        testApp.setUserId(1L);
        testApp.setCodeGenType(CodeGenTypeEnum.HTML.getValue());
        testApp.setCreateTime(LocalDateTime.now());
        testApp.setEditTime(LocalDateTime.now());
    }

    @Test
    void createApp_Success() {
        AppAddRequest request = new AppAddRequest();
        request.setInitPrompt("创建一个任务记录网站");

        when(appService.save(any())).thenAnswer(invocation -> {
            App savedApp = invocation.getArgument(0);
            savedApp.setId(1L);
            return true;
        });

        Long appId = appServiceImpl.createApp(request, testUser);

        assertNotNull(appId);
        verify(appService).save(any(App.class));
    }

    @Test
    void createApp_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appServiceImpl.createApp(null, testUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void createApp_NullInitPrompt_ThrowsException() {
        AppAddRequest request = new AppAddRequest();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appServiceImpl.createApp(request, testUser);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getAppVO_Success() {
        AppVO appVO = appServiceImpl.getAppVO(testApp);

        assertNotNull(appVO);
        assertEquals(testApp.getId(), appVO.getId());
        assertEquals(testApp.getAppName(), appVO.getAppName());
    }

    @Test
    void getAppVO_NullApp_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appServiceImpl.getAppVO(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getAppVOList_Success() {
        List<App> appList = Arrays.asList(testApp);

        List<AppVO> appVOList = appServiceImpl.getAppVOList(appList);

        assertNotNull(appVOList);
        assertEquals(1, appVOList.size());
        assertEquals(testApp.getAppName(), appVOList.get(0).getAppName());
    }

    @Test
    void getAppVOList_NullList_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appServiceImpl.getAppVOList(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getQueryWrapper_Success() {
        AppQueryRequest request = new AppQueryRequest();
        request.setAppName("测试");
        request.setUserId(1L);

        QueryWrapper wrapper = appServiceImpl.getQueryWrapper(request);

        assertNotNull(wrapper);
    }

    @Test
    void getQueryWrapper_NullRequest_ReturnsDefault() {
        QueryWrapper wrapper = appServiceImpl.getQueryWrapper(null);

        assertNotNull(wrapper);
    }

    @Test
    void deployApp_NotImplemented() {
        when(appService.getById(1L)).thenReturn(testApp);

        String deployUrl = appServiceImpl.deployApp(1L, testUser);

        assertNull(deployUrl);
    }
}
