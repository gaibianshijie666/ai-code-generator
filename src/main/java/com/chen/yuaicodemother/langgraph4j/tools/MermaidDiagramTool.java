package com.chen.yuaicodemother.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.chen.yuaicodemother.exception.BusinessException;
import com.chen.yuaicodemother.exception.ErrorCode;
import com.chen.yuaicodemother.langgraph4j.model.ImageResource;
import com.chen.yuaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.chen.yuaicodemother.utils.MinioUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MermaidDiagramTool {

//    @Resource
//    private CosManager cosManager;
    @Resource
    private MinioUtils minioUtils;
    
    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = minioUtils.uploadFile(diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将Mermaid代码转换为SVG图片
     */
//    private File convertMermaidToSvg(String mermaidCode) {
//        // 创建临时输入文件
//        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
//        FileUtil.writeUtf8String(mermaidCode, tempInputFile);
//        // 创建临时输出文件
//        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
//        // 根据操作系统选择命令
//        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
//        // 构建命令
//        String cmdLine = String.format("%s -i %s -o %s -b transparent",
//                command,
//                tempInputFile.getAbsolutePath(),
//                tempOutputFile.getAbsolutePath()
//        );
//        // 执行命令
//        RuntimeUtil.execForStr(cmdLine);
//        // 检查输出文件
//        if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败");
//        }
//        // 清理输入文件，保留输出文件供上传使用
//        FileUtil.del(tempInputFile);
//        return tempOutputFile;
//    }
    private File convertMermaidToSvg(String mermaidCode) {
        // 创建临时输入文件
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);

        try {
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);

            // 根据操作系统选择命令，优先使用 PATH 中的 mmdc
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";

            // 路径加双引号，避免空格或中文路径问题
            String cmdLine = String.format("%s -i \"%s\" -o \"%s\" -b transparent",
                    command,
                    tempInputFile.getAbsolutePath(),
                    tempOutputFile.getAbsolutePath()
            );

            // 执行命令并获取输出
            String output = RuntimeUtil.execForStr(cmdLine);
            log.info("Mermaid CLI 输出: {}", output);

            // 检查输出文件
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败: " + output);
            }

            return tempOutputFile;

        } finally {
            // 清理输入文件
            FileUtil.del(tempInputFile);
        }
    }

}
