package com.xuecheng.framework.domain.course;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author YHQ
 * @date 2020/1/9 18:46
 */

@Data
@ToString
@NoArgsConstructor
public class CourseView  implements Serializable {
    CourseBase courseBase;//基础信息
    CourseMarket courseMarket;//课程营销
    CoursePic coursePic;//课程图片
    com.xuecheng.framework.domain.course.ext.TeachplanNode TeachplanNode;//教学计划
}