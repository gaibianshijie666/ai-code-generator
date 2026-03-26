package com.chen.yuaicodemother.controller;

import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.common.DeleteRequest;
import com.chen.yuaicodemother.model.dto.user.*;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.model.vo.LoginUserVO;
import com.chen.yuaicodemother.model.vo.UserVO;
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

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testUser");
        testUser.setUserAccount("testAccount");
    }

    @Test
    void userRegister_Success() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount("testAccount");
        registerRequest.setUserPassword("password123");
        registerRequest.setCheckPassword("password123");

        when(userService.userRegister(anyString(), anyString(), anyString())).thenReturn(1L);

        BaseResponse<Long> response = userController.userRegister(registerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getData());
    }

    @Test
    void userRegister_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.userRegister(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void userLogin_Success() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUserAccount("testAccount");
        loginRequest.setUserPassword("password123");

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(1L);
        loginUserVO.setUserName("testUser");

        when(userService.userLogin(anyString(), anyString(), any())).thenReturn(loginUserVO);

        BaseResponse<LoginUserVO> response = userController.userLogin(loginRequest, null);

        assertNotNull(response);
        assertEquals("testUser", response.getData().getUserName());
    }

    @Test
    void userLogin_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.userLogin(null, null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getLoginUser_Success() {
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(1L);
        loginUserVO.setUserName("testUser");

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(userService.getLoginUserVO(any())).thenReturn(loginUserVO);

        BaseResponse<LoginUserVO> response = userController.getLoginUser(null);

        assertNotNull(response);
        assertEquals("testUser", response.getData().getUserName());
    }

    @Test
    void userLogout_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.userLogout(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void addUser_Success() {
        UserAddRequest addRequest = new UserAddRequest();
        addRequest.setUserName("新用户");
        addRequest.setUserAccount("newUser");

        User savedUser = new User();
        savedUser.setId(1L);

        when(userService.save(any())).thenReturn(true);
        when(userService.getEncryptPassword(anyString())).thenReturn("encrypted");

        BaseResponse<Long> response = userController.addUser(addRequest);

        assertNotNull(response);
    }

    @Test
    void addUser_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.addUser(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUserName("testUser");

        when(userService.getById(1L)).thenReturn(user);

        BaseResponse<User> response = userController.getUserById(1L);

        assertNotNull(response);
        assertEquals("testUser", response.getData().getUserName());
    }

    @Test
    void getUserById_InvalidId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.getUserById(0L);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userService.getById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.getUserById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), exception.getCode());
    }

    @Test
    void deleteUser_Success() {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(1L);

        when(userService.removeById(1L)).thenReturn(true);

        BaseResponse<Boolean> response = userController.deleteUser(deleteRequest);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void deleteUser_InvalidRequest_ThrowsException() {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.deleteUser(deleteRequest);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setUserName("更新后的用户名");

        when(userService.updateById(any())).thenReturn(true);

        BaseResponse<Boolean> response = userController.updateUser(updateRequest);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void updateUser_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.updateUser(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void updateUser_NullId_ThrowsException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.updateUser(updateRequest);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void listUserVOByPage_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userController.listUserVOByPage(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void save_Success() {
        User user = new User();
        user.setUserName("testUser");

        when(userService.save(any())).thenReturn(true);

        boolean result = userController.save(user);

        assertTrue(result);
    }

    @Test
    void remove_Success() {
        when(userService.removeById(1L)).thenReturn(true);

        boolean result = userController.remove(1L);

        assertTrue(result);
    }

    @Test
    void update_Success() {
        User user = new User();
        user.setId(1L);
        user.setUserName("updatedUser");

        when(userService.updateById(any())).thenReturn(true);

        boolean result = userController.update(user);

        assertTrue(result);
    }

    @Test
    void list_Success() {
        User user = new User();
        user.setId(1L);

        when(userService.list()).thenReturn(List.of(user));

        List<User> result = userController.list();

        assertEquals(1, result.size());
    }

    @Test
    void getInfo_Success() {
        User user = new User();
        user.setId(1L);

        when(userService.getById(1L)).thenReturn(user);

        User result = userController.getInfo(1L);

        assertEquals(1L, result.getId());
    }
}
