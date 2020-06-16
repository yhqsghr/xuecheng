package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常捕获
 * @author YHQ
 * @date 2019/12/29 15:03
 */
@ControllerAdvice//控制器增强
public class ExceptionCatch {
    //捕获指定异常，异常后会在这里被捕获
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult getCustomException(CustomException customException){
        //获取响应码
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

}
