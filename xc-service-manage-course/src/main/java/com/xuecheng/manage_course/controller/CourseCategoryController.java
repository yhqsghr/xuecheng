package com.xuecheng.manage_course.controller;
import com.xuecheng.api.course.CourseCategoryControllerApi;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author YHQ
 * @date 2020/1/9 9:17
 */
@RestController
@RequestMapping("/category")
public class CourseCategoryController implements CourseCategoryControllerApi {
    @Autowired
    CourseService courseService;

    /**
     * 查询课程分类
     *
     * @return
     */
    @Override
    @RequestMapping("/list")
    public CategoryNode findCourseCatrgory() {
        return courseService.findCourseCatrgory();
    }
}
