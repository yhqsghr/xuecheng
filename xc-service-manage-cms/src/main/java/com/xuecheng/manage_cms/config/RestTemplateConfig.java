package com.xuecheng.manage_cms.config;

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
    public RestTemplate RestTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
