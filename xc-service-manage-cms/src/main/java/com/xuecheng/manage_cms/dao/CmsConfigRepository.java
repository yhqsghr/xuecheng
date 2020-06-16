package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author YHQ
 * @date 2019/12/30 15:38
 */
public interface CmsConfigRepository extends MongoRepository<CmsConfig,String> {
}
