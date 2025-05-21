package com.example.backend.service.impl;

import com.example.backend.model.main2022;
import com.example.backend.service.Main2022ElasticSearchService;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Main2022ElasticSearchServiceImpl implements Main2022ElasticSearchService {

    @Autowired
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final NLPService nlpService;

    public Main2022ElasticSearchServiceImpl(ElasticsearchRestTemplate elasticsearchRestTemplate, NLPService nlpService) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.nlpService = nlpService;
    }


    public List<main2022> fullTextSearch(String query) {
        List<String> tokens = nlpService.tokenize(query);

        // 过滤掉空字符串、单个字母或标点符号的无效token
        tokens = tokens.stream()
                .filter(token -> token.length() > 1 && token.matches("[a-zA-Z0-9]+"))
                .collect(Collectors.toList());

        // 如果过滤后没有有效token，则返回空结果集
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        for (String token : tokens) {
            System.out.println("token: " + token);
        }

        // 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 遍历所有 token，对每个 token 构建一个 multi_match 查询
        for (String token : tokens) {
            if (isValidDate(token, "yyyy-MM-dd")) {
                MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(token, "sortdate");
                boolQueryBuilder.should(multiMatchQuery);
            } else {
                MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(token).
                        field("*");
                boolQueryBuilder.should(multiMatchQuery);
            }
        }

        // 使用 function_score 来基于匹配的关键词数量进行评分
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQueryBuilder,
                ScoreFunctionBuilders.weightFactorFunction(1.0f)
        ).boostMode(CombineFunction.SUM);

        // 创建 ElasticSearch 查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(functionScoreQuery)
                .withMinScore(1.0f)
                .withPageable(PageRequest.of(0, 50))
                .build();

        // 执行查询
        SearchHits<main2022> searchHits = elasticsearchRestTemplate.search(searchQuery, main2022.class);
        return searchHits.stream()
                .map(hit -> hit.getContent())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<main2022>> searchByKeywordAndDateRange(String keyword, String startDate, String endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 添加关键词搜索
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword)
                .field("subject_extended");
        boolQueryBuilder.should(multiMatchQuery);

        // 添加时间段过滤
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sortdate")
                .from(startDate)
                .to(endDate);
        boolQueryBuilder.filter(rangeQuery);

        // 按年份分类的聚合
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_year")
                .field("pubyear.keyword")
                .size(1000);

        // 创建 ElasticSearch 查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .addAggregation(aggregation)
                .build();
        // 执行查询
        SearchHits<main2022> searchHits = elasticsearchRestTemplate.search(searchQuery, main2022.class);

        // 获取聚合结果
        ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();

        // 提取按年份分类的聚合结果
        assert elasticsearchAggregations != null;
        Terms yearTerms = elasticsearchAggregations.aggregations().get("by_year");

        return yearTerms.getBuckets().stream()
                .collect(Collectors.toMap(
                        bucket -> bucket.getKeyAsString(),  // 年份作为键
                        bucket -> searchHits.stream()
                                .filter(hit -> hit.getContent().getPubyear().equals(bucket.getKeyAsString()))
                                .map(hit -> hit.getContent())
                                .collect(Collectors.toList())  // 对应年份的论文列表
                ));
    }


    private boolean isValidDate(String dateStr, String format) {
        try {
            // 解析日期字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            // 如果解析成功，则说明是一个合法的日期
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}