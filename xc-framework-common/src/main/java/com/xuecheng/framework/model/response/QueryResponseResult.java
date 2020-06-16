package com.xuecheng.framework.model.response;

import lombok.Data;
import lombok.ToString;

/**
 * 查询响应结果=查询结果+响应数据reslutCode
 * 在 ResponseResult 上封装了 查询响应集合
 */
@Data
@ToString
public class QueryResponseResult extends ResponseResult {
    //查询结果集
    QueryResult queryResult;

    //返回查询结果集和响应代码   有参构造
    public QueryResponseResult(ResultCode resultCode, QueryResult queryResult) {
        super(resultCode);
        this.queryResult = queryResult;
    }
}
