package com.chen.yuaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.exception.ThrowUtils;
import com.chen.yuaicodemother.model.entity.App;
import com.chen.yuaicodemother.service.AppService;
import com.chen.yuaicodemother.service.ScreenshotService;
import com.chen.yuaicodemother.utils.MinioUtils;
import com.chen.yuaicodemother.utils.WebScreenshotUtils;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

//    @Resource
//    private CosManager cosManager;
    @Resource
    private MinioUtils minioUtils;

    @Resource
    private AppService appService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        // 1. 生成本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        try {
            // 2. 上传到对象存储
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 3. 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成 COS 对象键
//        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
//        String cosKey = generateScreenshotKey(fileName);
//        return cosManager.uploadFile(cosKey, screenshotFile);
        return minioUtils.uploadFile(screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }

    @Override
    @Async
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        try {
            log.info("开始异步生成应用截图，appId: {}, appUrl: {}", appId, appUrl);
            String screenshotUrl = generateAndUploadScreenshot(appUrl);
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = appService.updateById(updateApp);
            if (!updated) {
                log.error("更新应用封面字段失败，appId: {}", appId);
            } else {
                log.info("应用截图更新成功，appId: {}, coverUrl: {}", appId, screenshotUrl);
            }
        } catch (Exception e) {
            log.error("异步生成应用截图失败，appId: {}, error: {}", appId, e.getMessage(), e);
            throw e;
        }
    }
}
