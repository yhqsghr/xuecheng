package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Category;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

/**
 * @author YHQ
 * @date 2020/1/9 9:12
 */
@Mapper
public interface CourseCategoryMapper {
    CategoryNode findAll();


    @Select("select name from category where id = #{mtid}")
    String findMtById(String mtid);

    @Select("select name from category where id = #{stid}")
    String findStById(String stid);

    /**
     * 根据mtname 和 stname 查询 mtid 和 stid
     * @param mt
     * @param st
     * @return
     */
    @Select("SELECT\n" +
            "\tc1.id mtid,\n" +
            "\tc2.id stid\n" +
            "FROM\n" +
            "\tcategory c1\n" +
            "\tLEFT JOIN category c2 ON c1.id = c2.parentid \n" +
            "WHERE\n" +
            "\tc1.NAME = #{mt} \n" +
            "\tAND\n" +
            "\tc2.NAME = #{st}")
    Map<String, String> getMtAndStByName(@Param("mt") String mt, @Param("st") String st);
}
