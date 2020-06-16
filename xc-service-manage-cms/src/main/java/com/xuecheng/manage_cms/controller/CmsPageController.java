package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YHQ
 * @date 2019/12/22 2:12
 */
@RequestMapping("/cms/page")
@RestController
public class CmsPageController implements CmsPageControllerApi {
    @Autowired
    private PageService pageService;


    /**
     * 发布页面
     *
     * @param pageId
     * @return
     */
    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return pageService.postPage(pageId);
    }
    /**
     * 一键发布页面
     */
    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postPageQuick(cmsPage);
    }

    /**
     * 分页查询 最好controller方法和service层是一样的方便调用
     *
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,
                                        @PathVariable("size") int size,
                                        QueryPageRequest queryPageRequest) {
        try {
            //执行成功
            //获取返回集合
            Page<CmsPage> pages = pageService.findList(page, size, queryPageRequest);
            QueryResult<CmsPage> queryResult = new QueryResult<>();

            //获取页面集合
            //page->list
            List<CmsPage> cmsPageList = pages.getContent();
            queryResult.setList(cmsPageList);
            //获取总记录数
            long totalPages = pages.getTotalElements();
            queryResult.setTotal(totalPages);
            return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        } catch (Exception e) {
            //执行失败
            e.printStackTrace();
            return new QueryResponseResult(CommonCode.FAIL, null);
        }

    }

    /**
     * 根据id查询cmspage回显页面
     *
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    @Override
    public CmsPage findById(@PathVariable("id") String id) {
        return pageService.findById(id);
    }


    /**
     * 根据cmspagename获取cmspage对象
     * @param pageName
     * @return
     */
    @GetMapping("/getCmsPage/{pageName}")
    @Override
    public CmsPage findByPageName(@PathVariable String pageName){
        return pageService.findByPageName(pageName);
    }

    /**
     * 删除存放在gridfs中的发布页面html
     * @param htmlFileId
     */
    @DeleteMapping("/deleteFile/{htmlFileId}")
    public void deleteHtmlFile(@PathVariable String htmlFileId){
        pageService.deleteHtmlFile(htmlFileId);
    }



    /**
     * 修改页面
     *
     * @param cmsPage
     * @return
     */
    @PutMapping("/edit/{id}")//这里使用put方法，http 方法中put表示更新
    @Override
    public CmsPageResult edit(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return pageService.edit(id, cmsPage);
    }

    /**
     * 添加页面
     */
    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }

    /**
     * course微服务调用接口添加页面
     */
    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return pageService.add(cmsPage);
    }

    /**
     * 删除页面
     *
     * @param id
     * @return
     */
    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable String id) {
        return pageService.delete(id);
    }

}
