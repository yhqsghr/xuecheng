package com.xuecheng.manage_course.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author YHQ
 * @date 2020/4/14 15:37
 */
@FeignClient(value = XcServiceList.XC_SERVICE_BASE_FILESYSTEM)
public interface FileSystemClient {
    @DeleteMapping("/filesystem/delete")
    ResponseResult delete(@RequestParam String fileId);
}
