package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author YHQ
 * @date 2020/1/9 20:26
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class testGridfsTemplate {
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Test
    public void test(){
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5a795bbcdd573c04508f3a59")));
        System.out.println(gridFSFile);
    }
}
