package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePub;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author YHQ
 * @date 2020/1/16 10:51
 */
public interface CoursePubRepository extends JpaRepository<CoursePub,String> {
}
