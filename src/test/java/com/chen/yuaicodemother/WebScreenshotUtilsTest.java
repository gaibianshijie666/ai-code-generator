package com.chen.yuaicodemother;

import com.chen.yuaicodemother.service.ScreenshotService;
import com.chen.yuaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class WebScreenshotUtilsTest {

    @Resource
    private ScreenshotService screenshotService;

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }

    @Test
    void saveWebPageScreenshotAsync() {
        String testUrl = "https://www.baidu.com/";
        screenshotService.generateAndUploadScreenshot(testUrl);
    }
}
