package com.xuecheng.filesystem.utils;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 赵老师亲撰fastdfs utils工具类
 * @author YHQ
 * @date 2020/1/6 20:26
 */
@Component
public class FastDFSClientWrapper {
    @Autowired
    private FastFileStorageClient storageClient;
    /**
     * @Description: 上传文件
     * @param file 文件对象
     * @return 文件路径
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return storePath.getFullPath();
    }

    /**
     * @Description: 上传文件
     * @param bytes 文件数据
     * @param format 文件格式（后缀）
     * @return String 文件路径
     */
    public String uploadFile(byte[] bytes, String format) {
        StorePath storePath = storageClient.uploadFile(new ByteArrayInputStream(bytes), bytes.length, format, null);
        return storePath.getFullPath();
    }

    /**
     * @Description: 上传文件
     * @param file 文件对象
     * @return
     * @throws IOException
     */
    public String uploadFile(File file) throws IOException {
        StorePath storePath = storageClient.uploadFile(FileUtils.openInputStream(file), file.length(),
                FilenameUtils.getExtension(file.getName()), null);
        return storePath.getFullPath();
    }


    /**
     * @Description: 根据文件路径下载文件
     * @param filePath 文件路径
     * @return 文件字节数据
     * @throws IOException
     */
    public byte[] downFile(String filePath) throws IOException {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        return storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), new DownloadCallback<byte[]>() {
            @Override
            public byte[] recv(InputStream ins) throws IOException {
                return org.apache.commons.io.IOUtils.toByteArray(ins);
            }
        });
    }

    /**
     * @Description: 根据文件地址删除文件
     * @param filePath 文件访问地址
     */
    public void deleteFile(String filePath) {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
    }
}
