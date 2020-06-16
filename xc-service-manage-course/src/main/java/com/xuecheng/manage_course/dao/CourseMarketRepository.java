package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author YHQ
 * @date 2020/1/9 18:51
 */
public interface CourseMarketRepository extends JpaRepository<CourseMarket,String> {
}
