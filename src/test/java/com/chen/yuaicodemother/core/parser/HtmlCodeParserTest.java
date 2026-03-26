package com.chen.yuaicodemother.core.parser;

import com.chen.yuaicodemother.ai.model.HtmlCodeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlCodeParserTest {

    private HtmlCodeParser htmlCodeParser;

    @BeforeEach
    void setUp() {
        htmlCodeParser = new HtmlCodeParser();
    }

    @Test
    void parseCode_WithHtmlCodeBlock_Success() {
        String codeContent = """
            ```html
            <!DOCTYPE html>
            <html>
            <head><title>Test</title></head>
            <body><h1>Hello World</h1></body>
            </html>
            ```
            """;

        HtmlCodeResult result = htmlCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
        assertTrue(result.getHtmlCode().contains("<h1>Hello World</h1>"));
    }

    @Test
    void parseCode_WithoutCodeBlock_Success() {
        String codeContent = """
            <!DOCTYPE html>
            <html>
            <body><h1>Hello</h1></body>
            </html>
            """;

        HtmlCodeResult result = htmlCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<h1>Hello</h1>"));
    }

    @Test
    void parseCode_EmptyContent_ReturnsEmptyString() {
        String codeContent = "";

        HtmlCodeResult result = htmlCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertEquals("", result.getHtmlCode());
    }

    @Test
    void parseCode_NullContent_ReturnsEmptyString() {
        assertThrows(NullPointerException.class, () -> {
            htmlCodeParser.parseCode(null);
        });
    }

    @Test
    void parseCode_WithLeadingTrailingWhitespace_Success() {
        String codeContent = "   \n   <html><body>Test</body></html>   \n   ";

        HtmlCodeResult result = htmlCodeParser.parseCode(codeContent);

        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<html>"));
    }
}
