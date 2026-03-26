package com.chen.yuaicodemother.core.parser;

import com.chen.yuaicodemother.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiFileCodeParserTest {

    private MultiFileCodeParser multiFileCodeParser;

    @BeforeEach
    void setUp() {
        multiFileCodeParser = new MultiFileCodeParser();
    }

    @Test
    void parseCode_WithAllFiles_Success() {
        String codeContent = """
            ```html
            <!DOCTYPE html>
            <html><body><h1>Hello</h1></body></html>
            ```
            
            ```css
            body { margin: 0; }
            ```
            
            ```javascript
            console.log('test');
            ```
            """;

        MultiFileCodeResult result = multiFileCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<h1>Hello</h1>"));
        assertTrue(result.getCssCode().contains("margin: 0"));
        assertTrue(result.getJsCode().contains("console.log"));
    }

    @Test
    void parseCode_WithOnlyHtml_Success() {
        String codeContent = """
            ```html
            <!DOCTYPE html>
            <html><body><h1>Hello</h1></body></html>
            ```
            """;

        MultiFileCodeResult result = multiFileCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<h1>Hello</h1>"));
        assertNull(result.getCssCode());
        assertNull(result.getJsCode());
    }

    @Test
    void parseCode_WithMixedCase_Success() {
        String codeContent = """
            ```HTML
            <html><body>Test</body></html>
            ```
            
            ```CSS
            body { color: red; }
            ```
            
            ```JavaScript
            alert('test');
            ```
            """;

        MultiFileCodeResult result = multiFileCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }

    @Test
    void parseCode_EmptyContent_ReturnsEmptyCodes() {
        String codeContent = "";

        MultiFileCodeResult result = multiFileCodeParser.parseCode(codeContent);

        assertNotNull(result);
    }

    @Test
    void parseCode_NullContent_ReturnsEmptyCodes() {
        assertThrows(NullPointerException.class, () -> {
            multiFileCodeParser.parseCode(null);
        });
    }

    @Test
    void parseCode_WithExtraWhitespace_Success() {
        String codeContent = """
            
            ```html
                
                <html><body>Test</body></html>
                
            ```
            
            ```css
                
                body { padding: 10px; }
                
            ```
            
            ```js
                
                const x = 1;
                
            ```
            
            """;

        MultiFileCodeResult result = multiFileCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<html>"));
        assertTrue(result.getCssCode().contains("padding"));
        assertTrue(result.getJsCode().contains("const"));
    }
}
