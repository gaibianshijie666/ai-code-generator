package com.chen.yuaicodemother.core.saver;

import com.chen.yuaicodemother.ai.model.HtmlCodeResult;
import com.chen.yuaicodemother.ai.model.MultiFileCodeResult;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeFileSaverExecutorTest {

    @Mock
    private HtmlCodeFileSaverTemplate htmlCodeFileSaver;

    @Mock
    private MultiFileCodeFileSaverTemplate multiFileCodeFileSaver;

    private CodeFileSaverExecutor codeFileSaverExecutor;

    @BeforeEach
    void setUp() {
        codeFileSaverExecutor = new CodeFileSaverExecutor(htmlCodeFileSaver, multiFileCodeFileSaver);
    }

    @Test
    void executeSaver_HtmlCode_Success() throws BusinessException {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        htmlCodeResult.setHtmlCode("<html><body>Test</body></html>");

        File expectedFile = new File("/tmp/test.html");
        when(htmlCodeFileSaver.saveCode(any(), anyLong())).thenReturn(expectedFile);

        File result = codeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, 1L);

        assertNotNull(result);
        assertEquals(expectedFile, result);
        verify(htmlCodeFileSaver).saveCode(htmlCodeResult, 1L);
    }

    @Test
    void executeSaver_MultiFileCode_Success() throws BusinessException {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        multiFileCodeResult.setHtmlCode("<html><body>Test</body></html>");
        multiFileCodeResult.setCssCode("body { margin: 0; }");
        multiFileCodeResult.setJsCode("console.log('test');");

        File expectedFile = new File("/tmp/test");
        when(multiFileCodeFileSaver.saveCode(any(), anyLong())).thenReturn(expectedFile);

        File result = codeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, 1L);

        assertNotNull(result);
        assertEquals(expectedFile, result);
        verify(multiFileCodeFileSaver).saveCode(multiFileCodeResult, 1L);
    }

    @Test
    void executeSaver_UnsupportedType_ThrowsException() {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            codeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.VUE_PROJECT, 1L);
        });
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void executeSaver_NullCodeGenType_ThrowsException() {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();

        assertThrows(NullPointerException.class, () -> {
            codeFileSaverExecutor.executeSaver(htmlCodeResult, null, 1L);
        });
    }
}
