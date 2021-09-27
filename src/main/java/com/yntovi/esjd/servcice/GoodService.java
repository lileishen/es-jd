package com.yntovi.esjd.servcice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.yntovi.esjd.pojo.Good;
import com.yntovi.esjd.utils.HtmlParseUtil;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoodService {
    @Resource
    private RestHighLevelClient restHighLevelClient;

//    1、解析数据放入es 中

    public  Boolean parseGood(String keyword) throws IOException {
        List<Good> goods = HtmlParseUtil.parseJd(keyword);
        //  把查询的数据放入es 中
        BulkRequest bulkRequest =new BulkRequest();
        bulkRequest.timeout("2m");
//        创建索引
        GetIndexRequest request = new GetIndexRequest("jd_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        if (exists==false) { //如果不存在
            restHighLevelClient.indices().create(new CreateIndexRequest("jd_index"),RequestOptions.DEFAULT);
        }
        for (Good good : goods) {
            bulkRequest.add(
                    new IndexRequest("jd_index")
                    .source(JSON.toJSONString(good),XContentType.JSON)
            );
        }
        return  !restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT).hasFailures();
    }

//    2、获取数据实现基本的搜索功能

    public  List<Map<String,Object>> searchPage(String keyWord,int page,int pageSize) throws IOException {
        page= page<=0?1:page;
//        条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        分页
         sourceBuilder.from(page);
         sourceBuilder.size(pageSize);
//      精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("name", keyWord);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        高亮
        HighlightBuilder highlightBuilder =new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.requireFieldMatch(false);// 关闭多个高亮
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
//      执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        解析结果
        ArrayList<Map<String, Object>> mapArrayList = new ArrayList<>();
        for (SearchHit documentFields: response.getHits().getHits()){
//            解析高亮字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Map<String, Object> map = documentFields.getSourceAsMap();
//            将原来的字段换成我们高亮的字段
            if (name!=null){
                Text[] fragments = name.fragments();
                  String newName="";
                for (Text fragment : fragments) {
                    newName+=fragment;
                }
                map.put("name",newName);
            }
            mapArrayList.add(map);
        }
        return  mapArrayList;
    }
}
