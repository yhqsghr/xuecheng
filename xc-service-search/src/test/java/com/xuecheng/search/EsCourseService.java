package com.xuecheng.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 要求 keyword搜索、分页（做）、排序(mt、st)、过滤、区间（价格做）、高亮（做）
     *
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    //课程搜索
    public QueryResponseResult list(int page, int size, CourseSearchParam courseSearchParam) {
        //参数校验
        if (courseSearchParam == null) {
            //如果为空构造新的参数
            courseSearchParam = new CourseSearchParam();
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        Float price_min = courseSearchParam.getPrice_min();
        Float price_max = courseSearchParam.getPrice_max();
        String keyword = courseSearchParam.getKeyword();
        String filter = courseSearchParam.getFilter();
        String grade = courseSearchParam.getGrade();
        String mt = courseSearchParam.getMt();
        String st = courseSearchParam.getSt();
        String sort = courseSearchParam.getSort();


        //创建返回对象
        QueryResult<CoursePub> coursePubQueryResult = new QueryResult<>();
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest();
        //创建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建boolquery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //创建muliMarchQueuy 根据关键字搜索
        MultiMatchQueryBuilder multiMatchQueryBuilder = null;
        if (StringUtils.isNotEmpty(keyword)) {
            //俩个字段中至少中一个才匹配;提高name字段的权重
            multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, "name", "teachplan","description").minimumShouldMatch("70%").field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //创建区间源并设置区间
        RangeQueryBuilder rangeQueryBuilder = null;
        if (price_min != null && price_max != null) {
            rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(price_max).lte(price_min);
            boolQueryBuilder.must(rangeQueryBuilder);
        }

        //bool过滤 相当于过滤出用户自己想要的结果所以应是精准查询
        if (StringUtils.isNotEmpty(mt)) {
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", mt));
        }
        if (StringUtils.isNotEmpty(st)) {
            //根据二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", st));
        }
        if (StringUtils.isNotEmpty(grade)) {
            //根据难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", grade));
        }

        //searchsourceBuilder设置属性
        searchSourceBuilder.size(size);
        searchSourceBuilder.from(page);
        //排序 sort->对应应该是字段名 例如：按price升序
        if (StringUtils.isNotEmpty(sort)) {
            searchSourceBuilder.sort(sort, SortOrder.ASC);
        }
        //过滤字段
        String[] field_Spilt = source_field.split(",");
        searchSourceBuilder.fetchSource(field_Spilt, new String[]{});
        //设置bool
        searchSourceBuilder.query(boolQueryBuilder);
        //高亮 对name teachplan高亮
        //searchSourceBuilder.highlighter(new HighlightBuilder().postTags("<tag>").preTags("</tag>").field("name"));
        //搜索对象整合搜索源
        searchRequest.indices(index);
        searchRequest.types(type);
        searchRequest.source(searchSourceBuilder);
        //查询操作
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            long totalHits = hits.totalHits;
            SearchHit[] document = hits.getHits();
            //创建用于接受index中的doument对象的list集合
            ArrayList<CoursePub> coursePubList = new ArrayList<>();
            for (SearchHit documentField : document) {
                //创建新的coursepub对象接受属性
                CoursePub coursePub = new CoursePub();
                //map -> list
                Map<String, Object> sourceAsMap = documentField.getSourceAsMap();
                //取出name
                String name = (String) sourceAsMap.get("name");
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if (sourceAsMap.get("price") != null) {
                        price = (Double) sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                //旧价格
                Double price_old = null;
                try {
                    if (sourceAsMap.get("price_old") != null) {
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                //将coursePub对象放入list
                coursePubList.add(coursePub);
            }
            //设置到结果集中
            coursePubQueryResult.setList(coursePubList);
            coursePubQueryResult.setTotal(totalHits);
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, coursePubQueryResult);
        return queryResponseResult;
    }
}
