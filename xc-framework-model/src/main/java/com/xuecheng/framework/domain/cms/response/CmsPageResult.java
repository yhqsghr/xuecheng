package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *cms微服务专有的查询页面返回的类 -> cmspage + resultCode(
 *     //操作是否成功,true为成功，false操作失败
 *     boolean success();
 *     //操作代码
 *     int code();
 *     //提示信息
 *     String message();
 *  )
 */
@Data
@NoArgsConstructor
public class CmsPageResult extends ResponseResult {
    CmsPage cmsPage;
    public CmsPageResult(ResultCode resultCode,CmsPage cmsPage) {
        super(resultCode);
        this.cmsPage = cmsPage;
    }
}
