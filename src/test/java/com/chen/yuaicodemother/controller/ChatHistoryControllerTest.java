package com.chen.yuaicodemother.controller;

import com.chen.yuaicodemother.common.BaseResponse;
import com.chen.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.chen.yuaicodemother.model.entity.ChatHistory;
import com.chen.yuaicodemother.model.entity.User;
import com.chen.yuaicodemother.service.ChatHistoryService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatHistoryControllerTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatHistoryController chatHistoryController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testUser");
    }

    @Test
    void listAllChatHistoryByPageForAdmin_NullRequest_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.listAllChatHistoryByPageForAdmin(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void listAppChatHistory_Success() {
        Long appId = 1L;
        int pageSize = 10;
        LocalDateTime lastCreateTime = LocalDateTime.now();

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(1L);
        chatHistory.setAppId(appId);

        Page<ChatHistory> page = new Page<>(1, pageSize, 1);
        page.setRecords(List.of(chatHistory));

        when(userService.getLoginUser(any())).thenReturn(testUser);
        when(chatHistoryService.listAppChatHistoryByPage(eq(appId), eq(pageSize), eq(lastCreateTime), any()))
            .thenReturn(page);

        BaseResponse<Page<ChatHistory>> response = chatHistoryController.listAppChatHistory(appId, pageSize, lastCreateTime, null);

        assertNotNull(response);
        assertNotNull(response.getData());
    }

    @Test
    void save_Success() {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(1L);

        when(chatHistoryService.save(any())).thenReturn(true);

        BaseResponse<Boolean> response = chatHistoryController.save(chatHistory);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void save_Null_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.save(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void remove_Success() {
        Long id = 1L;

        when(chatHistoryService.removeById(id)).thenReturn(true);

        BaseResponse<Boolean> response = chatHistoryController.remove(id);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void remove_InvalidId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.remove(0L);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void remove_NullId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.remove(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void update_Success() {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(1L);

        when(chatHistoryService.updateById(any())).thenReturn(true);

        BaseResponse<Boolean> response = chatHistoryController.update(chatHistory);

        assertNotNull(response);
        assertTrue(response.getData());
    }

    @Test
    void update_NullChatHistory_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.update(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void update_NullId_ThrowsException() {
        ChatHistory chatHistory = new ChatHistory();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.update(chatHistory);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getById_Success() {
        Long id = 1L;
        
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setId(id);
        chatHistory.setAppId(1L);

        when(chatHistoryService.getById(id)).thenReturn(chatHistory);

        BaseResponse<ChatHistory> response = chatHistoryController.getById(id);

        assertNotNull(response);
        assertEquals(id, response.getData().getId());
    }

    @Test
    void getById_InvalidId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.getById(0L);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getById_NullId_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatHistoryController.getById(null);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }
}
