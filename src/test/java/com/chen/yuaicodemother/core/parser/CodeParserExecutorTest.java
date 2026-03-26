package com.chen.yuaicodemother.core.parser;

import com.chen.yuaicodemother.ai.model.HtmlCodeResult;
import com.chen.yuaicodemother.ai.model.MultiFileCodeResult;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CodeParserExecutorTest {

    @Test
    void executeParser_HtmlCode_Success() {
        String htmlCode = """
            <!DOCTYPE html>
            <html>
            <head><title>Test</title></head>
            <body><h1>Hello World</h1></body>
            </html>
            """;

        Object result = CodeParserExecutor.executeParser(htmlCode, CodeGenTypeEnum.HTML);

        assertNotNull(result);
        assertInstanceOf(HtmlCodeResult.class, result);
        HtmlCodeResult htmlResult = (HtmlCodeResult) result;
        assertNotNull(htmlResult.getHtmlCode());
    }

    @Test
    void executeParser_MultiFileCode_Success() {
        String multiFileCode = """
            ```html
            <!DOCTYPE html>
            <html><body><h1>Index</h1></body></html>
            ```
            
            ```css
            body { margin: 0; }
            ```
            
            ```javascript
            console.log('Hello');
            ```
            """;

        Object result = CodeParserExecutor.executeParser(multiFileCode, CodeGenTypeEnum.MULTI_FILE);

        assertNotNull(result);
        assertInstanceOf(MultiFileCodeResult.class, result);
        MultiFileCodeResult multiFileResult = (MultiFileCodeResult) result;
        assertNotNull(multiFileResult.getHtmlCode());
    }

    @Test
    void executeParser_UnsupportedType_ThrowsException() {
        String code = "some code";

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            CodeParserExecutor.executeParser(code, CodeGenTypeEnum.VUE_PROJECT);
        });
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void executeParser_NullCodeGenType_ThrowsException() {
        String code = "some code";

        assertThrows(BusinessException.class, () -> {
            CodeParserExecutor.executeParser(code, null);
        });
    }
}
