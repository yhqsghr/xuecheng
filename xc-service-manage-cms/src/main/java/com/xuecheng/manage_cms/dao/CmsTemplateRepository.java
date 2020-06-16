package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author YHQ
 * @date 2019/12/31 9:33
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {
}
