package com.git.blog.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.git.blog.config.ElasticSearchConfig;
import com.git.blog.entity.Blog;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ESUtils {

    private final RestHighLevelClient restHighLevelClient;

    public ESUtils(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public void blog2ES(Blog blog) {
        IndexRequest indexRequest = new IndexRequest("blog");
        indexRequest.id(blog.getBlogId().toString());
        String jsonStr = JSONUtil.toJsonStr(blog);
        indexRequest.source(jsonStr, XContentType.JSON);
        try {
            restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteBlogFromES(Integer blogId) {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.index("blog");
        deleteRequest.id(blogId.toString());
        try {
            restHighLevelClient.delete(deleteRequest, ElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Object, Object> searchBlogFromES(String phrase, Integer page, List<Object> invisibilityType) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("blog");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(6).from(6 * page);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(phrase, "blogContent", "blogTitle", "blogAbstract"))
                .must(QueryBuilders.termQuery("blogState", 1));
        if (invisibilityType != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termsQuery("blogType", invisibilityType));
        }
        sourceBuilder.query(boolQueryBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchHits hits = searchResponse != null ? searchResponse.getHits() : null;
        long totalHits = hits != null ? hits.getTotalHits().value : 0;
        SearchHit[] searchHits = hits != null ? hits.getHits() : new SearchHit[0];
        List<Blog> blogs = new ArrayList<>();
        for (SearchHit hit: searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Blog blog = JSONUtil.toBean(sourceAsString, Blog.class);
            blogs.add(blog);
        }
        return MapUtil.builder()
                .put("blog", blogs)
                .put("totalHits", totalHits)
                .map();
    }
}
