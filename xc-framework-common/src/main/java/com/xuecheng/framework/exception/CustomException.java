package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * 自定义异常类
 * @author YHQ
 * @date 2019/12/29 15:02
 */
public class CustomException extends BaseException {
    //定义错误代码
    private ResultCode resultCode;
    //用构造方法给属性赋值
    public CustomException(ResultCode resultCode){
        this.resultCode=resultCode;
    }
    //取出错误代码
    public ResultCode getResultCode(){
        return resultCode;
    }
}
