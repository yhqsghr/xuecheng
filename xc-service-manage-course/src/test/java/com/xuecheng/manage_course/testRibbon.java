package com.xuecheng.manage_course;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.dao.FileSystemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * @author YHQ
 * @date 2020/1/8 19:39
// */
@SpringBootTest
@RunWith(SpringRunner.class)
public class testRibbon {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    FileSystemRepository fileSystemRepository;

    //负载均衡调用
    @Test
    public void testRibbon() {
        //服务id
        String serviceId = "xc-service-manage-cms";
        for(int i=0;i<10;i++){
            //通过服务id调用
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId + "/cms/page/get/5a754adf6abb500ad05688d9", CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            System.out.println(cmsPage);
        }
    }
    @Test
    public void testFileSystem(){
        fileSystemRepository.deleteById("group1/M00/00/00/rBDXNF6XHg6AetMCAADH4YU04lc606.png");
    }
}
