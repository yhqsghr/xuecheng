package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

/**
 * @author YHQ
 * @date 2020/1/5 21:06
 */
@RequestMapping("/course")
@RestController
public class CourseController implements CourseControllerApi {
    @Autowired
    CourseService courseService;
    /**
     * 查询课程计划
     */
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 添加课程计划
     *
     * @param teachplan
     * @return
     */
    @PostMapping("/teachplan/add")
    @Override
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    /**
     * 查询我的课程计划列表
     *
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult findCourseList(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            CourseListRequest courseListRequest) {
        return courseService.findCourseList(page, size, courseListRequest);
    }

    /**
     * 上传图片
     * 根据图片地址和课程名保存到course_pic中
     *
     * @param courseId
     * @param pic
     * @return
     */
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.addCoursePic(courseId, pic);
    }

    /**
     * 课程图片上传成功，再次进入课程上传页面应该显示出来已上传的图片。
     *
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursepic(courseId);

    }

    /**
     * 根据课程id删除图片
     *
     * @param courseId
     * @return
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /**
     * 课程页面视图（预览对象） -> dataurl对应的数据对象
     * @param id
     * @return
     */
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.courseview(id);
    }

    /**
     * 预览课程页面
     * @param id
     * @return
     */
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    /**
     * 调用cms一键发布接口
     * @param id
     * @return
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable String id) {
        return courseService.publish(id);
    }

    /**
     * 添加课程
     * @param courseBase
     * @return
     */
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    /**
     * 根据课程id查找页面返回（回显）
     * @param courseId
     * @return
     * @throws RuntimeException
     */
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
        return courseService.getCoursebaseById(courseId);
    }

    /**
     * 根据课程id修改 course课程
     * @param courseId
     * @param courseForm
     * @return
     */
    @Override
    @PutMapping("/coursebase/update/{courseId}")
    public ResponseResult updateCourseBase(@PathVariable("courseId") String courseId, @RequestBody CourseBase courseForm) {
        return courseService.updateCoursebase(courseId,courseForm);
    }

    /**
     * 修改营销
     * @param courseId
     * @param courseMarket
     * @return
     */
    @Override
    @PutMapping("/coursemarket/update/{courseId}")
    public ResponseResult updateCourseMarket(@PathVariable("courseId") String courseId, @RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket_u = courseService.updateCourseMarket(courseId, courseMarket);
        if(courseMarket_u!=null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else{
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    /**
     * 根据teachplanId修改nodeName
     * @param teachplanid
     * @param nodeName
     * @return
     */
    @Override
    @PutMapping("/courseplan/edit")
    public ResponseResult updateCoursePlan(@RequestParam String teachplanid, @RequestParam String nodeName) {
        return courseService.updateCoursePlan(teachplanid,nodeName);
    }

    /**
     * 根据课程计划id删除课程plan
     * @param nodeId
     * @return
     */
    @Override
    @RequestMapping("/coursePlan/delete/{nodeId}")
    public ResponseResult deleteCourseNode(@PathVariable String nodeId) {
        return courseService.deleteCourseNode(nodeId);
    }

    /**
     * 根据课程id删除课程信息
     * @param courseId
     * @return
     */
    @Override
    @DeleteMapping("/course/delete/{courseId}")
    public ResponseResult deleteCourse(@PathVariable String courseId) {
        return courseService.deleteCourse(courseId);
    }



    /**
     * 根据课程id查询课程营销
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }


}
