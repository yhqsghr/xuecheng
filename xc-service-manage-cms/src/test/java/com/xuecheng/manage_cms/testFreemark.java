package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author YHQ
 * @date 2020/1/9 19:06
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class testFreemark {
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 测试上传文件到gridfs中 保存模板数据
     *
     * @return
     */
    @Test
    public void saveTemplate() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\course.ftl"));
        ObjectId courseTemplate = gridFsTemplate.store(fileInputStream, "courseTemplate");
        String s = courseTemplate.toHexString();
        //返回的是文件id
        System.out.println(s);
    }
}
