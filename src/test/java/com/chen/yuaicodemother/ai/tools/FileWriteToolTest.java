package com.chen.yuaicodemother.ai.tools;

import com.chen.yuaicodemother.common.constant.AppConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileWriteToolTest {

    private FileWriteTool fileWriteTool;

    @BeforeEach
    void setUp() {
        fileWriteTool = new FileWriteTool();
    }

    @Test
    void writeFile_NullPath_ReturnsError() {
        try (MockedStatic<AppConstant> mocked = mockStatic(AppConstant.class)) {
            mocked.when(AppConstant::getCodeOutputRootDir).thenReturn("/tmp");
            
            String result = fileWriteTool.writeFile(null, "content", 1L);

            assertTrue(result.contains("不能为空"));
        }
    }

    @Test
    void writeFile_EmptyPath_ReturnsError() {
        try (MockedStatic<AppConstant> mocked = mockStatic(AppConstant.class)) {
            mocked.when(AppConstant::getCodeOutputRootDir).thenReturn("/tmp");
            
            String result = fileWriteTool.writeFile("", "content", 1L);

            assertTrue(result.contains("不能为空"));
        }
    }
}
