package com.chen.yuaicodemother.controller;

import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.common.DeleteRequest;
import com.chen.yuaicodemother.model.dto.app.*;
import com.chen.yuaicodemother.model.entity.App;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.AppVO;
import com.chen.yuaicodemother.service.AppService;
import com.chen.yuaicodemother.service.ProjectDownloadService;
import com.chen.yuaicodemother.service.UserService;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.mybatisflex.core.paginate.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AppControllerTest {

    @Mock
    private AppService appService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectDownloadService projectDownloadService;

    @InjectMocks
    private AppController appController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testUser");
        testUser.setUserRole("user");
    }

    @Test
    void addApp_Success() {
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个任务记录网站");

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(appService.createApp(any(), any())).thenReturn(1L);

        BaseResponse<Long> response = appController.addApp(appAddRequest, null);

        assertNotNull(response);
        assertEquals(1L, response.getData());
        verify(appService).createApp(any(), eq(testUser));
    }

    @Test
    void addApp_NullRequest_ThrowsException() {
        assertThrows(BusinessException.class, () -> {
            appController.addApp(null, null);
        });
    }

    @Test
    void updateApp_Success() {
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(1L);
        appUpdateRequest.setAppName("更新后的应用名称");

        App existingApp = new App();
        existingApp.setId(1L);
        existingApp.setUserId(1L);

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(appService.getById(1L)).thenReturn(existingApp);
        when(appService.updateById(any())).thenReturn(true);

        BaseResponse<Boolean> response = appController.updateApp(appUpdateRequest, null);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void updateApp_NotOwner_ThrowsException() {
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(1L);

        App existingApp = new App();
        existingApp.setId(1L);
        existingApp.setUserId(999L);

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(appService.getById(1L)).thenReturn(existingApp);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appController.updateApp(appUpdateRequest, null);
        });
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    @Test
    void deleteApp_Success() {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(1L);

        App existingApp = new App();
        existingApp.setId(1L);
        existingApp.setUserId(1L);

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(appService.getById(1L)).thenReturn(existingApp);
        when(appService.removeById(1L)).thenReturn(true);

        BaseResponse<Boolean> response = appController.deleteApp(deleteRequest, null);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void deleteApp_AdminCanDeleteOtherUserApp() {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(1L);

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUserRole("admin");

        App existingApp = new App();
        existingApp.setId(1L);
        existingApp.setUserId(1L);

        when(userService.getLoginUser(any())).thenReturn(adminUser);
        when(appService.getById(1L)).thenReturn(existingApp);
        when(appService.removeById(1L)).thenReturn(true);

        BaseResponse<Boolean> response = appController.deleteApp(deleteRequest, null);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void getAppVOById_Success() {
        App app = new App();
        app.setId(1L);
        app.setAppName("测试应用");
        app.setUserId(1L);

        AppVO appVO = new AppVO();
        appVO.setId(1L);
        appVO.setAppName("测试应用");

        when(appService.getById(1L)).thenReturn(app);
        when(appService.getAppVO(app)).thenReturn(appVO);

        BaseResponse<AppVO> response = appController.getAppVOById(1L);

        assertNotNull(response);
        assertEquals("测试应用", response.getData().getAppName());
    }

    @Test
    void getAppVOById_NotFound_ThrowsException() {
        when(appService.getById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appController.getAppVOById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getAppVOById_InvalidId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appController.getAppVOById(0L);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void listMyAppVOByPage_PageSizeExceedsLimit_ThrowsException() {
        AppQueryRequest queryRequest = new AppQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(100);

        when(userService.getLoginUser(any())).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appController.listMyAppVOByPage(queryRequest, null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void deployApp_InvalidAppId_ThrowsException() {
        AppDeployRequest deployRequest = new AppDeployRequest();
        deployRequest.setAppId(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appController.deployApp(deployRequest, null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }
}
