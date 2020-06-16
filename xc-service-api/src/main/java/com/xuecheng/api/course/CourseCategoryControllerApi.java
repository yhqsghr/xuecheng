package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.Category;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * @author YHQ
 * @date 2020/1/9 9:17
 */
@Api(value = "课程分类管理",description = "课程分类管理",tags = {"课程分类管理"})
public interface CourseCategoryControllerApi {
    @ApiOperation("查询课程分类")
    public CategoryNode findCourseCatrgory();
}
