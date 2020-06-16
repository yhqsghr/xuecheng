package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author YHQ
 * @date 2020/1/7 15:31
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String> {
}
