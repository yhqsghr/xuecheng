package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.HashMap;

/**
 * @author YHQ
 * @date 2020/1/5 21:05
 */
public interface TeachplanRepository extends JpaRepository<Teachplan, String> {

    Teachplan findByCourseidAndParentid(String courseId,String parentId);
}
