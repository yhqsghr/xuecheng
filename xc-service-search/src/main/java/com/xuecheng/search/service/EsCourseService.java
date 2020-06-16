package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.media.index}")
    private String media_index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.media.type}")
    private String media_type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;


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


        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);

        //创建返回对象
        QueryResult<CoursePub> coursePubQueryResult = new QueryResult<>();

        //创建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建boolquery查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //创建muliMarchQueuy 根据关键字搜索
        if (StringUtils.isNotEmpty(keyword)) {
            //俩个字段中至少中一个才匹配;提高name字段的权重
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
                    .multiMatchQuery(keyword, "name", "teachplan", "description")
                    .minimumShouldMatch("70%").field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //创建区间源并设置区间
        if (price_min != null && price_max != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders
                    .rangeQuery("price")
                    .gte(price_max)
                    .lte(price_min);
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
        int from = (page - 1) * size;
        //每页显示的记录数
        searchSourceBuilder.size(size);
        //起始记录下标从0开始 第一页从0个记录开始（0-9） ，第二页从10开始（10-19）
        searchSourceBuilder.from(from);
        //过滤字段
        String[] field_Spilt = source_field.split(",");
        //保留前面的 舍弃后面的
        searchSourceBuilder.fetchSource(field_Spilt, new String[]{});
        //排序 sort->对应应该是字段名 例如：按price升序
        if (StringUtils.isNotEmpty(sort)) {
            searchSourceBuilder.sort(sort, SortOrder.ASC);
        }
        //设置bool
        searchSourceBuilder.query(boolQueryBuilder);
        //高亮 对name teachplan高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().postTags("</font>").preTags("<font class='eslight'>");
        List<HighlightBuilder.Field> fields = highlightBuilder.fields();
        //对name和teachplan进行高亮设置
        fields.add(new HighlightBuilder.Field("name"));
        //fields.add(new HighlightBuilder.Field("teachplan"));
        searchSourceBuilder.highlighter(highlightBuilder);
        //搜索对象整合搜索源
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
                //课程id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出高亮字段内容
                Map<String, HighlightField> highlightFields = documentField.getHighlightFields();
                if (highlightFields != null) {
                    HighlightField nameField = highlightFields.get("name");
                    if (nameField != null) {
                        Text[] fragments = nameField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text str : fragments) {
                            stringBuffer.append(str.string());
                        }
                        name = stringBuffer.toString();
                    }
                }
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
            return new QueryResponseResult(CommonCode.FAIL, null);
        }

        return new QueryResponseResult(CommonCode.SUCCESS, coursePubQueryResult);
    }

    /**
     * 根据course_id查询课程发布表信息
     * @param id
     * @return
     */
    //使用ES的客户端向ES请求查询索引信息
    public Map<String, CoursePub> getall(String id) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //指定type
        searchRequest.types(type);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termQuery
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        //过虑源字段，不用设置源字段，取出所有字段
//        searchSourceBuilder.fetchSource()
        searchRequest.source(searchSourceBuilder);
        //最终要返回的课程信息

        Map<String,CoursePub> map = new HashMap<>();
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit hit:searchHits){
                CoursePub coursePub = new CoursePub();
                //获取源文档的内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //课程id
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据courseid获取教学计划发布表信息
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult getmedia(String[] teachplanIds) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //指定type
        searchRequest.types(media_type);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termsQuery根据多个id 查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过虑源字段
        String[] includes = media_source_field.split(",");
        searchSourceBuilder.fetchSource(includes,new String[]{});
        searchRequest.source(searchSourceBuilder);
        //使用es客户端进行搜索请求Es
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            //执行搜索
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            total = hits.totalHits;
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit hit:searchHits){
                TeachplanMediaPub teachplanMediaPub= new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                teachplanMediaPubList.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //数据集合
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;

    }
}
