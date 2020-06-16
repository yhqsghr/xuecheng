package com.xuecheng.api.cms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
//import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * @author YHQ
 * @date 2020/1/4 20:27
 */
@Api(value = "页面预览")
public interface CmsPagePreviewControllerApi {
    @ApiOperation(value = "页面预览")
    public void preview(String pageId) throws IOException;
}
