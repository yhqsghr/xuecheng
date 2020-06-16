package com.xuecheng.manage_course.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author YHQ
 * @date 2019/12/30 16:03
 */
@Configuration
public class RestTemplateConfig {
    @Bean
    //开启ribben负载均衡
    @LoadBalanced
    public RestTemplate RestTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
