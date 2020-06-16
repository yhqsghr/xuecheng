package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.Map;
import java.util.Optional;

/**
 * 在cms client工程的mq包下创建ConsumerPostPage类，
 * ConsumerPostPage作为发布页面的消费客户端，监听页面发布队列的消息，收到消息后从mongodb下载文件，保存在本地。
 * @author YHQ
 * @date 2020/1/3 19:48
 */
@Component
public class ConsumerPostPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    PageService pageService;

    /**
     * 监听该队列下的消息
     */
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        //根据页面Id取出值
        String pageId = (String) map.get("pageId");
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
        if (cmsPage.isPresent()){
            //如果页面存在,则根据页面Id保存页面到物理路径下
            pageService.savePageToServerPath(pageId);
        }else{
            //记录日志
            LOGGER.error("cmsPage is null :{}",msg.toString());
        }

    }

}
