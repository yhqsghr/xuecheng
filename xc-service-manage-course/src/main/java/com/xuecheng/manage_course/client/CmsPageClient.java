package com.xuecheng.manage_course.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author YHQ
 * @date 2020/1/8 20:41
 */
//声明这是一个Fegin客户端 同时通过value指定服务名称
//value的值为 要请求的微服务在eureka中的注册名
@FeignClient(value = XcServiceList.XC_SERVICE_MANAGE_CMS)
public interface CmsPageClient {

    @GetMapping("/cms/page/get/{id}")
    //pathVariable()和 requestParam()里一定要写参数名
    public CmsPage findById(@PathVariable("id") String id);

    //保存cmspage页面 根据courseView里的对象内容生成模板数据,再根据cmspageid找到模板
    @PostMapping("/cms/page/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage);

    @PostMapping("/cms/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage);

    //根据pageid删除页面
    @DeleteMapping("/cms/page/del/{id}")
    public ResponseResult delete(@PathVariable String id);

    //根据页面id删除存放在gridfs中的html发布页面
    @DeleteMapping("/cms/page/deleteFile/{htmlFileId}")
    public void deleteHtmlFile(@PathVariable String htmlFileId);

    //一键发布页面
    @PostMapping("/cms/page/postPageQuick")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);

    //根据pagename获取cmpage
    @GetMapping("/cms/page/getCmsPage/{pageName}")
    public CmsPage findByPageName(@PathVariable String pageName);
}


