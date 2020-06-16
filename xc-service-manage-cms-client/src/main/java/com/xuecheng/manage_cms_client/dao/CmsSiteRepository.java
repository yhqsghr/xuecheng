package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 使用CmsSiteRepository查询站点信息，主要获取站点物理路径
 * @author YHQ
 * @date 2020/1/3 19:41
 */

public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
