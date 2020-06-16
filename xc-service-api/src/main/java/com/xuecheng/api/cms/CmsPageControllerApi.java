package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * 定义cms系统查询页面接口
 * 尽量把注解定义在接口中保持实现类的整洁
 *
 * @author YHQ
 * @date 2019/12/22 2:10
 */
@Api(value = "cms页面管理接口，提供页面的增删改查")
@Validated//要使用validate进行验证需要加上此注解
public interface CmsPageControllerApi {
    /**
     * 采用restful风格请求
     * 本项目中定义所有查询列表结果统一定义为QueryResponseResult 区别于CmsPageResult
     * 关系结构：站点->模板->页面
     * 此处查询出来的是多个cms管理页面所以用list集合接收，区别于CmsPageResult只是代表一个页面和其响应结果
     * 且所有响应结果都继承 ResponseResult 类 ->  实现Response接口
     * <p>
     * 返回查询结果和响应代码
     * QueryResponseResult(注入queryResult(list结果集+total总数)+resultCode) -> ResponseResult -> Response
     * <p>
     * 返回单个cmspage页面结果和响应代码
     * CmsPageResult(cmsPage+resultCode) -> ResponseResult -> Response
     *
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */

    //描述一个类的一个方法，或者说一个接口
    @ApiOperation(value = "分页查询页面列表")
    //多个请求参数
    @ApiImplicitParams({
            //一个请求参数
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
    })
    //当传入的参数是bean类型 需要加上@Valid 注解开启此bean类中的注解功能(如果此bean下已开启注解，没有也不影响)
    QueryResponseResult findList(@Min(1) int page,
                                 @Min(10) int size,
                                 @Valid QueryPageRequest queryPageRequest);

    /**
     * 修改cms页面
     */
    @ApiOperation("根据id查询页面")
    CmsPage findById(String id);

    @ApiOperation(value = "修改页面")
    CmsPageResult edit(String id, CmsPage cmsPage);

    /**
     * 增加
     */
    @ApiOperation(value = "添加页面")
    CmsPageResult add(CmsPage cmsPage);

    /**
     * 删除
     */
    @ApiOperation(value = "删除页面")
    ResponseResult delete(String id);

    /**
     * 页面发布
     *
     * @param pageId
     * @return
     */
    @ApiOperation(value = "页面发布")
    public ResponseResult post(String pageId);


    /**
     * 一键发布页面
     *
     * @param cmsPage
     * @return
     */
    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);


    /**
     * cours微服务调用保存页面
     *
     * @param cmsPage
     * @return
     */
    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("根据pageName获取cmspage")
    public CmsPage findByPageName(@PathVariable String pageName);
}
