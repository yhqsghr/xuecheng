package com.xuecheng.filesystem;

import com.xuecheng.filesystem.dao.FileSystemRepository;
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
    FileSystemRepository fileSystemRepository;

    @Test
    public void testFileSystem(){
        fileSystemRepository.deleteById("group1/M00/00/00/rBDXNF6XJkOAVBmGAABKy_gjL64630.jpg");
    }
}
