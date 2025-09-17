package com.chen.yuaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.chen.yuaicodemother.mapper")
public class YuAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeMotherApplication.class, args);
    }

}
