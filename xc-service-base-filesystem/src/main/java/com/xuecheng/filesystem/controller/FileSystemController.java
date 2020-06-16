package com.xuecheng.filesystem.controller;

import com.xuecheng.api.filesystem.FileSystemControllerApi;
import com.xuecheng.filesystem.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author YHQ
 * @date 2020/1/7 15:28
 */
@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi {
    @Autowired
    FileSystemService fileSystemService;

    /**
     * 文件系统服务对外暴漏储存图片接口
     *
     * @param multipartFile
     * @param filetag
     * @param businesskey
     * @param metadata
     * @return
     */
    @Override
    @PostMapping("/upload")
    public UploadFileResult upload(@RequestParam("file") MultipartFile multipartFile,
                                   @RequestParam(value = "filetag", required = true) String filetag,
                                   @RequestParam(value = "businesskey", required = false) String businesskey,
                                   @RequestParam(value = "metedata", required = false) String metadata) {
        return fileSystemService.upload(multipartFile, filetag, businesskey, metadata);
    }

    /**
     * 根据文件路径删除文件
     * @param fileId
     * @return
     */
    @Override
    @DeleteMapping("/delete")
    public ResponseResult delete(@RequestParam String fileId) {
        return fileSystemService.delete(fileId);
    }
}
