package com.example.backend.service.impl;

import com.example.backend.model.main2022;
import com.example.backend.service.Main2022ElasticSearchService;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Main2022ElasticSearchServiceImpl implements Main2022ElasticSearchService {

    @Autowired
    private final ElasticsearchOperations elasticsearchOperations;
    private final NLPService nlpService;

    public Main2022ElasticSearchServiceImpl(ElasticsearchOperations elasticsearchOperations, NLPService nlpService) {
        this.elasticsearchOperations = elasticsearchOperations;
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

        try {
            // 构建布尔查询
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 遍历所有 token，对每个 token 构建查询
            for (String token : tokens) {
                if (isValidDate(token, "yyyy-MM-dd")) {
                    boolQueryBuilder.should(MatchQuery.of(m -> m
                            .field("sortdate")
                            .query(token)
                    )._toQuery());
                } else {
                    boolQueryBuilder.should(WildcardQuery.of(w -> w
                            .field("keyword")
                            .value("*" + token.toLowerCase() + "*")
                    )._toQuery());
                    boolQueryBuilder.should(WildcardQuery.of(w -> w
                            .field("article_title")
                            .value("*" + token.toLowerCase() + "*")
                    )._toQuery());
                }
            }

            // 创建查询
            NativeQuery searchQuery = new NativeQueryBuilder()
                    .withQuery(boolQueryBuilder.build()._toQuery())
                    .withPageable(PageRequest.of(0, 50))
                    .build();

            // 执行查询
            SearchHits<main2022> searchHits = elasticsearchOperations.search(searchQuery, main2022.class);
            return searchHits.stream()
                    .map(hit -> hit.getContent())
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("全文搜索失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<main2022>> searchByKeywordAndDateRange(String keyword, String startDate, String endDate) {
        System.out.println("开始执行搜索 - 关键词: " + keyword + ", 开始年份: " + startDate + ", 结束年份: " + endDate);

        try {
            // 首先测试基本连接
            if (!elasticsearchOperations.indexOps(main2022.class).exists()) {
                System.err.println("索引 'main' 不存在");
                return new TreeMap<>();
            }

            // 构建查询
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                BoolQuery.Builder keywordQueryBuilder = new BoolQuery.Builder();

                // 使用wildcard查询
                keywordQueryBuilder.should(WildcardQuery.of(w -> w
                        .field("subject_extended")
                        .value("*" + keyword.toLowerCase() + "*")
                        .boost(3.0f)
                )._toQuery());

                keywordQueryBuilder.should(WildcardQuery.of(w -> w
                        .field("article_title")
                        .value("*" + keyword.toLowerCase() + "*")
                        .boost(2.5f)
                )._toQuery());

                keywordQueryBuilder.should(WildcardQuery.of(w -> w
                        .field("abstract_text")
                        .value("*" + keyword.toLowerCase() + "*")
                        .boost(2.0f)
                )._toQuery());

                keywordQueryBuilder.should(WildcardQuery.of(w -> w
                        .field("keyword")
                        .value("*" + keyword.toLowerCase() + "*")
                        .boost(2.5f)
                )._toQuery());

                keywordQueryBuilder.should(WildcardQuery.of(w -> w
                        .field("keyword_plus")
                        .value("*" + keyword.toLowerCase() + "*")
                        .boost(2.0f)
                )._toQuery());

                // 添加匹配查询作为备选
                keywordQueryBuilder.should(MatchQuery.of(m -> m
                        .field("subject_extended")
                        .query(keyword)
                        .boost(2.0f)
                )._toQuery());

                keywordQueryBuilder.should(MatchQuery.of(m -> m
                        .field("article_title")
                        .query(keyword)
                        .boost(1.5f)
                )._toQuery());

                keywordQueryBuilder.minimumShouldMatch("1");
                boolQueryBuilder.must(keywordQueryBuilder.build()._toQuery());
            }

            // 时间范围过滤
            if (startDate != null && endDate != null) {
                boolQueryBuilder.filter(RangeQuery.of(r -> r
                        .field("pubyear")
                        .gte(JsonData.of(startDate))
                        .lte(JsonData.of(endDate))
                )._toQuery());
            }

            System.out.println("构建的查询完成");

            // 先执行一个测试查询
            NativeQuery testQuery = new NativeQueryBuilder()
                    .withPageable(PageRequest.of(0, 5))
                    .build();

            SearchHits<main2022> testHits = elasticsearchOperations.search(testQuery, main2022.class);
            System.out.println("测试查询返回记录数: " + testHits.getTotalHits());

            if (testHits.getTotalHits() == 0) {
                System.err.println("警告：索引中没有任何数据");
                return new TreeMap<>();
            }

            // 打印一些样本数据用于调试
            testHits.stream().limit(2).forEach(hit -> {
                main2022 sample = hit.getContent();
                System.out.println("样本数据 - pubyear: " + sample.getPubyear() +
                        ", keyword: " + (sample.getKeyword() != null ? sample.getKeyword().substring(0, Math.min(50, sample.getKeyword().length())) : "null") +
                        ", title: " + (sample.getArticle_title() != null ? sample.getArticle_title().substring(0, Math.min(50, sample.getArticle_title().length())) : "null"));
            });

            // 创建主查询
            NativeQuery searchQuery = new NativeQueryBuilder()
                    .withQuery(boolQueryBuilder.build()._toQuery())
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            // 执行查询
            SearchHits<main2022> searchHits = elasticsearchOperations.search(searchQuery, main2022.class);
            List<main2022> allPapers = searchHits.stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

            System.out.println("主查询返回的论文总数: " + allPapers.size());

            if (allPapers.isEmpty()) {
                // 如果主查询没有结果，尝试更宽松的查询
                System.out.println("主查询无结果，尝试更宽松的查询...");

                BoolQuery.Builder relaxedQuery = new BoolQuery.Builder();

                // 只使用关键词查询，不限制时间
                if (keyword != null && !keyword.trim().isEmpty()) {
                    relaxedQuery.should(WildcardQuery.of(w -> w
                            .field("article_title")
                            .value("*" + keyword.toLowerCase() + "*")
                    )._toQuery());

                    relaxedQuery.should(WildcardQuery.of(w -> w
                            .field("keyword")
                            .value("*" + keyword.toLowerCase() + "*")
                    )._toQuery());

                    relaxedQuery.should(MatchQuery.of(m -> m
                            .field("article_title")
                            .query(keyword)
                    )._toQuery());

                    relaxedQuery.should(MatchQuery.of(m -> m
                            .field("keyword")
                            .query(keyword)
                    )._toQuery());

                    relaxedQuery.minimumShouldMatch("1");
                }

                NativeQuery relaxedSearchQuery = new NativeQueryBuilder()
                        .withQuery(relaxedQuery.build()._toQuery())
                        .withPageable(PageRequest.of(0, 100))
                        .build();

                SearchHits<main2022> relaxedHits = elasticsearchOperations.search(relaxedSearchQuery, main2022.class);
                List<main2022> relaxedPapers = relaxedHits.stream()
                        .map(hit -> hit.getContent())
                        .collect(Collectors.toList());

                System.out.println("宽松查询返回的论文数: " + relaxedPapers.size());

                if (!relaxedPapers.isEmpty()) {
                    // 手动过滤年份范围
                    if (startDate != null && endDate != null) {
                        relaxedPapers = relaxedPapers.stream()
                                .filter(paper -> {
                                    String pubyear = paper.getPubyear();
                                    if (pubyear != null && pubyear.matches("\\d{4}")) {
                                        int year = Integer.parseInt(pubyear);
                                        int start = Integer.parseInt(startDate);
                                        int end = Integer.parseInt(endDate);
                                        return year >= start && year <= end;
                                    }
                                    return false;
                                })
                                .collect(Collectors.toList());
                    }

                    allPapers = relaxedPapers;
                    System.out.println("过滤后的论文数: " + allPapers.size());
                }
            }

            // 手动按年份分组
            Map<String, List<main2022>> result = allPapers.stream()
                    .filter(paper -> paper.getPubyear() != null && !paper.getPubyear().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            paper -> paper.getPubyear(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            System.out.println("最终结果按年份分组: " + result.keySet());
            System.out.println("各年份论文数量: ");
            result.forEach((year, papers) -> {
                System.out.println("  " + year + ": " + papers.size() + " 篇");
            });

            return result;

        } catch (Exception e) {
            System.err.println("Elasticsearch 查询失败: " + e.getMessage());
            e.printStackTrace();

            return new TreeMap<>();
        }
    }

    /**
     * 调试方法：检查Elasticsearch连接和数据
     */
    public void debugElasticsearchConnection() {
        try {
            System.out.println("=== Elasticsearch调试信息 ===");

            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(main2022.class).exists();
            System.out.println("索引是否存在: " + indexExists);

            if (indexExists) {
                // 获取总文档数
                NativeQuery countQuery = new NativeQueryBuilder().build();
                long totalCount = elasticsearchOperations.count(countQuery, main2022.class);
                System.out.println("总文档数: " + totalCount);

                if (totalCount > 0) {
                    // 获取样本数据
                    NativeQuery sampleQuery = new NativeQueryBuilder()
                            .withPageable(PageRequest.of(0, 3))
                            .build();

                    SearchHits<main2022> sampleHits = elasticsearchOperations.search(sampleQuery, main2022.class);
                    System.out.println("样本数据:");
                    sampleHits.forEach(hit -> {
                        main2022 doc = hit.getContent();
                        System.out.println("  pubyear: '" + doc.getPubyear() + "'");
                        System.out.println("  keyword前50字符: '" +
                                (doc.getKeyword() != null ? doc.getKeyword().substring(0, Math.min(50, doc.getKeyword().length())) : "null") + "'");
                    });

                    // 检查特定年份的数据
                    NativeQuery yearQuery = new NativeQueryBuilder()
                            .withQuery(RangeQuery.of(r -> r
                                    .field("pubyear")
                                    .gte(JsonData.of("2020"))
                                    .lte(JsonData.of("2024"))
                            )._toQuery())
                            .build();

                    long yearCount = elasticsearchOperations.count(yearQuery, main2022.class);
                    System.out.println("2020-2024年份范围内的文档数: " + yearCount);
                }
            }

        } catch (Exception e) {
            System.err.println("调试过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidDate(String dateStr, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}