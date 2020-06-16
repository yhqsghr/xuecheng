package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.filesystem.utils.FastDFSClientWrapper;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.TextScore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.xuecheng.framework.domain.filesystem.response.FileSystemCode.FS_UPLOADFILE_FILEISNULL;

/**
 * @author YHQ
 * @date 2020/1/7 15:28
 */
@Service
@Transactional
public class FileSystemService {
    @Autowired
    FileSystemRepository fileSystemRepository;
    @Autowired
    FastDFSClientWrapper fastDFSClientWrapper;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param filetag
     * @param businesskey
     * @param metadata
     * @return
     */
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        //校验
        if (multipartFile.isEmpty()) {
            //文件为空
            ExceptionCast.cast(FS_UPLOADFILE_FILEISNULL);
        }

        try {
            //返回已上传文件name
            String fileId = fastDFSClientWrapper.uploadFile(multipartFile);

            //创建文件对象 （信息储存在mongodb中）
            FileSystem fileSystem = new FileSystem();
            fileSystem.setFileId(fileId);
            fileSystem.setFilePath(fileId);
            fileSystem.setFileName(multipartFile.getOriginalFilename());
            fileSystem.setFileSize(multipartFile.getSize());
            fileSystem.setFileType(multipartFile.getContentType());
            if (StringUtils.isNotEmpty(filetag)) {
                fileSystem.setFiletag(filetag);
            }
            if (StringUtils.isNotEmpty(businesskey)) {
                fileSystem.setBusinesskey(businesskey);
            }
            if (StringUtils.isNotEmpty(metadata)) {
                //把String -> map集合
                Map<String, Object> map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            }
            //保存fileSystem到mongodb数据库
            fileSystemRepository.save(fileSystem);
            return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
        } catch (IOException e) {
            e.printStackTrace();
            return new UploadFileResult(CommonCode.FAIL, null);
        }
    }

    /**
     * 根据文件路径删除文件
     * @param fileId
     * @return
     */
    public ResponseResult delete(String fileId) {
        if (StringUtils.isEmpty(fileId)){
            return new ResponseResult(FileSystemCode.FS_UPLOADFILE_BUSINESSISNULL);
        }
        String fileUrl = "http://47.98.149.200:8001/"+fileId;
        fastDFSClientWrapper.deleteFile(fileUrl);
        return new ResponseResult(CommonCode.SUCCESS);
    }

}








































