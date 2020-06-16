package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

/**
 * @author YHQ
 * @date 2020/1/12 20:11
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;


    //搜索全部记录
    // public final SearchResponse search(SearchRequest searchRequest, Header... headers)
    @Test
    public void testSearchAll() throws IOException {
        String[] indices = {"xc_course", "demo"};
        //搜索全部数据 ,并设置过滤(匹配通配符 * )
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()).fetchSource("*", "description");
        //查询指定的索引和类型中的document文件
        SearchRequest searchRequest = new SearchRequest().indices(indices).types("doc").source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hitsList = hits.getHits();
        for (SearchHit document : hitsList) {
            Map<String, Object> sourceAsMap = document.getSourceAsMap();
            System.out.println(sourceAsMap);
            System.out.println("==================");
        }
    }

    //分页查询
    @Test
    public void testPage() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //搜索全部数据
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        //设置分页数据
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //模糊查询
    //@Test
    //public void testSearchByCondition() throws IOException {
    //    //索引库
    //    String[] indices = {"xc_course"};
    //    //构造SearchSourceBuilder
    //    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.fuzzyQuery("name", "spri"));
    //    //创建搜索请求对象
    //    SearchRequest searchRequest = new SearchRequest();
    //    searchRequest.source(searchSourceBuilder);
    //    searchRequest.indices(indices);
    //    SearchResponse search = client.search(searchRequest);
    //    SearchHits hits = search.getHits();
    //    SearchHit[] hisList = hits.getHits();
    //    for (SearchHit documentFields : hisList) {
    //        System.out.println(documentFields.getSourceAsMap());
    //        System.out.println("=============");
    //    }
    //}


    //termquery精确查询不对搜索关键词进行分词
    @Test
    public void testTermQuery() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //terms可以设置多个参数 不是字段注意区别
        //SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.termQuery("name","基础"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.termsQuery("name", "spring", "开发"));
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //termsquery根据id查询
    @Test
    public void testTermQueryByid() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.termQuery("_id", "1"));
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //matchquery 对搜索关键字分词
    @Test
    public void testMatchQuery() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //设置spring\开发同时要匹配
        //SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchQuery("name","spring开发").operator(Operator.AND));
        //设置最小应匹配 如： 有30%的概率命中即可 即三个分词 只要有一个中即可
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchQuery("name", "spring开发java").operator(Operator.OR).minimumShouldMatch("30%"));
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //多字段匹配查询 multiMatchQuery termquery和mactchquery只能单字段查询 mulitmatchquery的出现就是为了 实现多字段查询
    @Test
    public void multiMatchQuery() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //多字段query  fieldNames设置多个字段 .filed boost给字段设置权重
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("大数据", "name", "description").operator(Operator.OR).minimumShouldMatch("30%").field("name", 10));
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //布尔查询 + 过滤器(在查询的结果上再次过滤条件查询出来的条件)
    //可以理解为把多个查询关联起来
    // must：表示必须，多个查询条件必须都满足。（通常使用must）
    // should：表示或者，多个查询条件只要有一个满足即可。
    // must_not：表示非
    @Test
    public void boolquery() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //精确查询
        TermQueryBuilder termQuery = QueryBuilders.termQuery("studymodel", "201001");
        //多字段匹配查询
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("开发", "description", "name").minimumShouldMatch("50%").operator(Operator.OR).field("name", 10);
        SearchSourceBuilder BoolSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.boolQuery().must(termQuery).must(multiMatchQuery).filter(QueryBuilders.termQuery("name", "开发")));//相当于不过滤（保留）name = ****的结果 和子查询类似，拿着结果当条件
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(BoolSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //排序
    @Test
    public void testPaiXu() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        //多字段匹配查询
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("开发", "description", "name").minimumShouldMatch("50%").operator(Operator.OR).field("name", 10);
        //查询分数字段40-100
        SearchSourceBuilder BoolSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.boolQuery().must(multiMatchQuery).filter(QueryBuilders.rangeQuery("price").gte("40").lte("100")));
        //创建搜索请求对象
        BoolSourceBuilder.sort("price", SortOrder.DESC);
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(BoolSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            System.out.println(documentFields.getSourceAsMap());
            System.out.println("=============");
        }
    }

    //高亮显示
    @Test
    public void testhighlight() throws IOException {
        //索引库
        String[] indices = {"xc_course"};
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //多字段匹配查询
        //查询分数字段40-100
        searchSourceBuilder.query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.multiMatchQuery("开发", "description", "name").minimumShouldMatch("50%").operator(Operator.OR).field("name", 10))
                        .filter(QueryBuilders.rangeQuery("price").gte("40").lte("100")));
        //排序
        searchSourceBuilder.sort("price", SortOrder.DESC);
        //高亮
        searchSourceBuilder.highlighter(new HighlightBuilder().preTags("<前缀>").postTags("</后缀>").field("name"));
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置索引
        searchRequest.indices(indices);
        SearchResponse search = client.search(searchRequest);
        System.out.println("搜索tookTime:"+search.getTook());
        System.out.println("一共搜索了个"+search.getTotalShards()+"分区");
        System.out.println("resonse对象："+search.toString());
        SearchHits hits = search.getHits();
        SearchHit[] hisList = hits.getHits();
        for (SearchHit documentFields : hisList) {
            //取出
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField highlightField = highlightFields.get("name");
            if (highlightField==null){
                break;
            }
            Text[] fragments = highlightField.getFragments();
            StringBuffer stringBuffer = new StringBuffer();
            for (Text fragment : fragments) {
                stringBuffer.append(fragment.toString());
            }
            name = stringBuffer.toString();
            System.out.println(sourceAsMap);
            System.out.println("=============");
        }
    }
}













































