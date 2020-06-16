package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author YHQ
 * @date 2020/1/3 19:40
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
}