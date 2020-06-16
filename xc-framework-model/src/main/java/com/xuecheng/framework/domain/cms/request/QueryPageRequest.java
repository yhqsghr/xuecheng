package com.xuecheng.framework.domain.cms.request;

import lombok.Data;

/**
 * cms前端查询条件封装
 *
 * @author YHQ
 * @date 2019/12/22
 */
@Data
public class QueryPageRequest {
    //站点id
    //@NotNull(message = "请求站点id不能为空")
    private String siteId;

    //页面ID
    private String pageId;

    //页面名称
    private String pageName;

    //别名
    private String pageAliase;

    //页面类型
    private String pageType;

    //模版id
    private String templateId;
}
