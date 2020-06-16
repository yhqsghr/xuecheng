package com.xuecheng.framework.domain.course;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by admin on 2018/2/7.
 */
@Data
@ToString
@Entity//声明我是一个实体告诉jpa框架我和数据库中表是对应的v
@Table(name="teachplan")
@JsonInclude(JsonInclude.Include.NON_NULL)//jackson自带方法使得返回的json数据中有null的不返回
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")//主键生成策略
public class Teachplan implements Serializable {
    private static final long serialVersionUID = -916357110051689485L;
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String id;
    private String pname;
    private String parentid;
    private String grade;
    private String ptype;
    private String description;
    private String courseid;
    private String status;
    private Integer orderby;
    private Double timelength;
    private String trylearn;

}
