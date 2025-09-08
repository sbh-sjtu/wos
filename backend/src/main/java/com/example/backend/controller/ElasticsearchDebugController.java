package com.example.backend.controller;

import com.example.backend.model.main2022;
import com.example.backend.service.impl.Main2022ElasticSearchServiceImpl;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/debug")
@CrossOrigin
public class ElasticsearchDebugController {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private Main2022ElasticSearchServiceImpl elasticSearchService;

    /**
     * 检查Elasticsearch连接状态
     */
    @GetMapping("/connection")
    public Map<String, Object> checkConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(main2022.class).exists();
            result.put("indexExists", indexExists);

            if (indexExists) {
                // 获取总文档数
                long totalCount = elasticsearchOperations.count(Query.findAll(), main2022.class);
                result.put("totalDocuments", totalCount);

                result.put("status", "SUCCESS");
            } else {
                result.put("status", "INDEX_NOT_EXISTS");
                result.put("message", "索引 'main' 不存在");
            }

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取样本数据
     */
    @GetMapping("/sample")
    public Map<String, Object> getSampleData() {
        Map<String, Object> result = new HashMap<>();

        try {
            NativeQuery sampleQuery = new NativeQueryBuilder()
                    .withPageable(PageRequest.of(0, 5))
                    .build();

            SearchHits<main2022> sampleHits = elasticsearchOperations.search(sampleQuery, main2022.class);

            List<Map<String, Object>> samples = new ArrayList<>();
            sampleHits.forEach(hit -> {
                main2022 doc = hit.getContent();
                Map<String, Object> sample = new HashMap<>();
                // 使用wos_uid替代seq_temp
                sample.put("wos_uid", doc.getWos_uid());
                sample.put("pubyear", doc.getPubyear());
                sample.put("article_title", doc.getArticle_title() != null ?
                        doc.getArticle_title().substring(0, Math.min(100, doc.getArticle_title().length())) : null);
                sample.put("keyword", doc.getKeyword() != null ?
                        doc.getKeyword().substring(0, Math.min(100, doc.getKeyword().length())) : null);
                sample.put("subject_extended", doc.getSubject_extended());
                samples.add(sample);
            });

            result.put("samples", samples);
            result.put("sampleCount", samples.size());
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 测试关键词搜索
     */
    @GetMapping("/test-keyword")
    public Map<String, Object> testKeywordSearch(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("测试关键词搜索: " + keyword);

            // 测试不同的查询方式
            List<Map<String, Object>> queryResults = new ArrayList<>();

            // 1. 测试wildcard查询
            NativeQuery wildcardQuery = new NativeQueryBuilder()
                    .withQuery(WildcardQuery.of(w -> w
                            .field("keyword")
                            .value("*" + keyword.toLowerCase() + "*")
                    )._toQuery())
                    .withPageable(PageRequest.of(0, 5))
                    .build();

            SearchHits<main2022> wildcardHits = elasticsearchOperations.search(wildcardQuery, main2022.class);
            queryResults.add(Map.of("queryType", "wildcard_keyword", "hits", wildcardHits.getTotalHits()));

            // 2. 测试match查询
            NativeQuery matchQuery = new NativeQueryBuilder()
                    .withQuery(MatchQuery.of(m -> m
                            .field("keyword")
                            .query(keyword)
                    )._toQuery())
                    .withPageable(PageRequest.of(0, 5))
                    .build();

            SearchHits<main2022> matchHits = elasticsearchOperations.search(matchQuery, main2022.class);
            queryResults.add(Map.of("queryType", "match_keyword", "hits", matchHits.getTotalHits()));

            // 3. 测试article_title搜索
            NativeQuery titleQuery = new NativeQueryBuilder()
                    .withQuery(WildcardQuery.of(w -> w
                            .field("article_title")
                            .value("*" + keyword.toLowerCase() + "*")
                    )._toQuery())
                    .withPageable(PageRequest.of(0, 5))
                    .build();

            SearchHits<main2022> titleHits = elasticsearchOperations.search(titleQuery, main2022.class);
            queryResults.add(Map.of("queryType", "wildcard_title", "hits", titleHits.getTotalHits()));

            result.put("queryResults", queryResults);
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 测试年份范围查询
     */
    @GetMapping("/test-date-range")
    public Map<String, Object> testDateRange(@RequestParam String startYear, @RequestParam String endYear) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("测试年份范围查询: " + startYear + " - " + endYear);

            // 1. 测试基本年份范围查询
            NativeQuery rangeQuery = new NativeQueryBuilder()
                    .withQuery(RangeQuery.of(r -> r
                            .field("pubyear")
                            .gte(JsonData.of(startYear))
                            .lte(JsonData.of(endYear))
                    )._toQuery())
                    .withPageable(PageRequest.of(0, 10))
                    .build();

            SearchHits<main2022> rangeHits = elasticsearchOperations.search(rangeQuery, main2022.class);
            result.put("rangeQueryHits", rangeHits.getTotalHits());

            // 2. 获取年份分布
            Map<String, Integer> yearDistribution = new HashMap<>();
            rangeHits.forEach(hit -> {
                String year = hit.getContent().getPubyear();
                yearDistribution.merge(year, 1, Integer::sum);
            });

            result.put("yearDistribution", yearDistribution);
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取字段统计信息
     */
    @GetMapping("/field-stats")
    public Map<String, Object> getFieldStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            NativeQuery sampleQuery = new NativeQueryBuilder()
                    .withPageable(PageRequest.of(0, 100))
                    .build();

            SearchHits<main2022> sampleHits = elasticsearchOperations.search(sampleQuery, main2022.class);

            Map<String, Object> fieldStats = new HashMap<>();
            int totalDocs = 0;
            int pubyearNotNull = 0;
            int keywordNotNull = 0;
            int titleNotNull = 0;
            int subjectNotNull = 0;
            int wosUidNotNull = 0;

            Set<String> uniqueYears = new HashSet<>();

            for (var hit : sampleHits) {
                main2022 doc = hit.getContent();
                totalDocs++;

                // 统计wos_uid不为空的数量（替代seq_temp）
                if (doc.getWos_uid() != null && !doc.getWos_uid().trim().isEmpty()) {
                    wosUidNotNull++;
                }

                if (doc.getPubyear() != null && !doc.getPubyear().trim().isEmpty()) {
                    pubyearNotNull++;
                    uniqueYears.add(doc.getPubyear());
                }
                if (doc.getKeyword() != null && !doc.getKeyword().trim().isEmpty()) {
                    keywordNotNull++;
                }
                if (doc.getArticle_title() != null && !doc.getArticle_title().trim().isEmpty()) {
                    titleNotNull++;
                }
                if (doc.getSubject_extended() != null && !doc.getSubject_extended().trim().isEmpty()) {
                    subjectNotNull++;
                }
            }

            fieldStats.put("totalSampleDocs", totalDocs);
            fieldStats.put("wosUidNotNullCount", wosUidNotNull); // 替代seq_temp统计
            fieldStats.put("pubyearNotNullCount", pubyearNotNull);
            fieldStats.put("keywordNotNullCount", keywordNotNull);
            fieldStats.put("titleNotNullCount", titleNotNull);
            fieldStats.put("subjectNotNullCount", subjectNotNull);
            fieldStats.put("uniqueYears", new ArrayList<>(uniqueYears));

            result.put("fieldStats", fieldStats);
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 测试完整的学科分析查询
     */
    @PostMapping("/test-full-query")
    public Map<String, Object> testFullQuery(@RequestParam String keyword,
                                             @RequestParam String startYear,
                                             @RequestParam String endYear) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("测试完整查询 - 关键词: " + keyword + ", 年份: " + startYear + "-" + endYear);

            // 调用实际的搜索方法
            Map<String, List<main2022>> searchResult = elasticSearchService.searchByKeywordAndDateRange(keyword, startYear, endYear);

            result.put("searchResultSize", searchResult.size());
            result.put("yearKeys", searchResult.keySet());

            Map<String, Integer> yearCounts = new HashMap<>();
            searchResult.forEach((year, papers) -> {
                yearCounts.put(year, papers.size());
            });

            result.put("yearCounts", yearCounts);
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 新增：测试多表查询功能
     */
    @GetMapping("/test-multi-table")
    public Map<String, Object> testMultiTableQuery() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里可以添加测试多表查询的逻辑
            result.put("message", "多表查询测试功能");
            result.put("supportedYearRange", "1970-1979");
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}