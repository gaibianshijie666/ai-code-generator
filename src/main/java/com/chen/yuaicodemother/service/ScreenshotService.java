package com.chen.yuaicodemother.service;

public interface ScreenshotService {
    String generateAndUploadScreenshot(String webUrl);

    /**
     * 异步生成应用截图并更新封面
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);
}
