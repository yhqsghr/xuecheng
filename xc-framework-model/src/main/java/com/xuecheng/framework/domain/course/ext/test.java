package com.xuecheng.framework.domain.course.ext;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.course.Category;

/**
 * @author YHQ
 * @date 2020/1/11 11:53
 */
public class test {
    public static void main(String[] args) {
        Category category = new Category();
        category.setName("1");
        category.setLabel("1");
        String s = JSON.toJSONString(category);
        System.out.println(s);
    }
}
