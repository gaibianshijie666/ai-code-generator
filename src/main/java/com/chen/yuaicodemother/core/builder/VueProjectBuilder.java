package com.chen.yuaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {

    private final Executor asyncExecutor;

    public VueProjectBuilder(@Qualifier("asyncTaskExecutor") Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 异步构建项目
     */
    public void buildProjectAsync(String projectPath){
        asyncExecutor.execute(() -> {
            try{
                buildProject(projectPath);
            }catch (Exception e){
                log.error("异步构建Vue项目时发生异常：{}",e.getMessage(),e);
            }
        });
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }


    /**
     * 操作系统检测方法
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * 根据操作系统构造命令方法
     */
    private String buildCommand(String baseCommand) {
        if(isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 构建Vue项目
     *
     * @param projectPath
     * @return
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if(!projectDir.exists()||!projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }
        //检测package.json是否存在
        File packageJson = new File(projectDir, "package.json");
        if(!packageJson.exists()||!packageJson.isFile()) {
            log.error("package.json文件不存在：{}",packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建Vue项目：{}",projectPath);
        //执行npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        //执行npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build执行失败");
            return false;
        }
        //验证dist目录是否存在
        File distDir = new File(projectDir, "dist");
        if(!distDir.exists()||!distDir.isDirectory()) {
            log.error("构建完成，但dist目录未生成：{}",distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue项目构建成功，dist目录：{}",distDir.getAbsolutePath());
        return true;
    }


    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

}
