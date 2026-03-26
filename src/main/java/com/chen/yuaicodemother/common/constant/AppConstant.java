package com.chen.yuaicodemother.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用常量配置
 */
@Component
public class AppConstant {

    public static final Integer GOOD_APP_PRIORITY = 99;

    public static final Integer DEFAULT_APP_PRIORITY = 0;

    private static String codeOutputRootDir;
    private static String codeDeployRootDir;
    private static String codeDeployHost;

    @Value("${app.code.output-dir:${user.dir}/tmp/code_output}")
    public void setCodeOutputRootDir(String codeOutputRootDir) {
        AppConstant.codeOutputRootDir = codeOutputRootDir;
    }

    @Value("${app.code.deploy-dir:${user.dir}/tmp/code_deploy}")
    public void setCodeDeployRootDir(String codeDeployRootDir) {
        AppConstant.codeDeployRootDir = codeDeployRootDir;
    }

    @Value("${app.code.deploy-host:http://localhost}")
    public void setCodeDeployHost(String codeDeployHost) {
        AppConstant.codeDeployHost = codeDeployHost;
    }

    public static String getCodeOutputRootDir() {
        if (codeOutputRootDir == null) {
            throw new IllegalStateException("AppConstant 未初始化，请确保 ApplicationContext 已启动");
        }
        return codeOutputRootDir;
    }

    public static String getCodeDeployRootDir() {
        if (codeDeployRootDir == null) {
            throw new IllegalStateException("AppConstant 未初始化，请确保 ApplicationContext 已启动");
        }
        return codeDeployRootDir;
    }

    public static String getCodeDeployHost() {
        if (codeDeployHost == null) {
            throw new IllegalStateException("AppConstant 未初始化，请确保 ApplicationContext 已启动");
        }
        return codeDeployHost;
    }
}
