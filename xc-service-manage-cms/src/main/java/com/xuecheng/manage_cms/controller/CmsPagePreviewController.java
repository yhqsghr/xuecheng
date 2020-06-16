package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPagePreviewControllerApi;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
//import java.util.Map;

/**
 * 页面发布controller
 * @author Administrator
 * @version 1.0
 * @create 2018-09-15 16:20
 **/
@Controller
public class CmsPagePreviewController extends BaseController implements CmsPagePreviewControllerApi {

    @Autowired
    PageService pageService;

    //页面预览
    @Override
    @RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
    public void preview(@PathVariable("pageId") String pageId) throws IOException {
        //执行静态化
        String pageHtml = pageService.getHtml(pageId);
        //通过response对象将内容输出
        ServletOutputStream outputStream = response.getOutputStream();
        //响应内容设置为html类型 解析ssi
        response.setHeader("Content-type","text/html;charset=utf-8");
        outputStream.write(pageHtml.getBytes("utf-8"));
    }
}
