package com.xuecheng.framework.domain.filesystem;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Map;

/**
 * Created by mrt on 2018/2/5.
 */
@Data
@ToString
@Document(collection = "filesystem")
public class FileSystem {

    //表示表中主键
    @Id
    //jpa默认主键生成策略
    //@GeneratedValue()
    private String fileId;
    //文件请求路径
    private String filePath;
    //文件大小
    private long fileSize;
    //文件名称
    private String fileName;
    //文件类型
    private String fileType;
    //图片宽度
    private int fileWidth;
    //图片高度
    private int fileHeight;

    //用户id，用于授权
    private String userId;
    //业务key
    private String businesskey;
    //业务标签
    private String filetag;
    //文件元信息
    private Map metadata;

}
