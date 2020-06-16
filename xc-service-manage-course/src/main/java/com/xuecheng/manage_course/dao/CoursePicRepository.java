package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author YHQ
 * @date 2020/1/7 22:20
 */
public interface CoursePicRepository extends JpaRepository<CoursePic, String> {
}
