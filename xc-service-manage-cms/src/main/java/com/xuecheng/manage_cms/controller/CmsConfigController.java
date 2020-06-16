package com.xuecheng.manage_cms.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.xuecheng.api.cms.CmsConfigControllerApi;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author YHQ
 * @date 2019/12/30 15:44
 */

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping("/cms/config")
public class CmsConfigController implements CmsConfigControllerApi {
    @Autowired
    PageService pageService;
    @Autowired
    GridFSBucket gridFSBucket;

    @Override
    @ResponseBody
    @GetMapping("/getmodel/{id}")
    public CmsConfig getmodel(@PathVariable("id") String id) {
        return pageService.getConfigById(id);
    }

    ////测试从gridfs系统中读取和写入模板文件
    //@Autowired
    //static GridFsTemplate gridFsTemplate;
    //@Autowired
    //static GridFSBucket gridFSBucket;
    //
    //public static void main(String[] args) throws IOException {
    //    //存模板文件
    //    //定义file
    //    File file = new File("d:/index_banner.html");
    //    //定义fileInputStream
    //    FileInputStream fileInputStream = new FileInputStream(file);
    //    ObjectId objectId = gridFsTemplate.store(fileInputStream, "index_banner.html");
    //    System.out.println(objectId);
    //
    //
    //    //取文件
    //    //根据文件id查询文件
    //    GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5ad8a54d68db5240b42e5fee")));
    //
    //    //打开一个下载流对象
    //    GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
    //    //创建GridFsResource对象，获取流
    //    GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
    //    //从流中取数据
    //    String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
    //    System.out.println(content);
    //
    //}
}