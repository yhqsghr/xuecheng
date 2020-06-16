package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

/**
 * @author YHQ
 * @date 2020/1/3 19:42
 */
@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * service层提供根据页面id,将发布的页面(已经静态化到gridfs中的html页面)储存(替换)到服务器真实的物理路径下
     *
     * @param pageId
     */
    public void savePageToServerPath(String pageId) {
        //1.从gridfs中取出已经静态化的页面
        InputStream inputStream = this.getHtmlByPageId(pageId);
        //2.保存到原页面所在的物理路径下 页面物理路径=站点物理路径+页面物理路径+页面名称
        //取出页面的信息
        Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(pageId);
        if (cmsPageOptional == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = cmsPageOptional.get();
        String pagePhysicalPath = cmsPage.getPagePhysicalPath();
        String pageName = cmsPage.getPageName();
        //拼接后的页面物理路径
        String physicalPagePath = pagePhysicalPath + pageName;

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(physicalPagePath));
            //使用IOutils重写页面
            IOUtils.copy(inputStream, fileOutputStream);
            //储存成功 - > 储存信息到redis
            //redisTemplate
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关流
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据pageid -> htmlFileId -> 文件内容
     *
     * @param pageId
     * @return
     */
    public InputStream getHtmlByPageId(String pageId) {
        //取出页面的信息
        Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(pageId);
        if (cmsPageOptional == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = cmsPageOptional.get();
        //获取文件ID
        String htmlFileId = cmsPage.getHtmlFileId();
        //获取存在gridfs中的文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFsResource对象，获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //从流中取数据
        try {
            InputStream inputStream = gridFsResource.getInputStream();
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
