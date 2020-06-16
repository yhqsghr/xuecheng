package com.xuecheng.manage_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author YHQ
 * @date 2019/12/22 23:58
 */

@SpringBootApplication
@EnableDiscoveryClient//开启发现客户端发现
@EntityScan("com.xuecheng.framework.domain.cms")//扫描@Document注解的实体类

@ComponentScan(basePackages = {"com.xuecheng.api"})//扫描接口

@ComponentScan(basePackages = {"com.xuecheng.manage_cms"})//扫描本项目下的所有类

@ComponentScan(basePackages = {"com.xuecheng.framework"})//扫描通用接口
public class ManageCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsApplication.class, args);
    }
}