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
import java.util.*;
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
        // 构建更复杂的查询，支持多字段搜索
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 关键词搜索 - 在多个字段中搜索
        BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
        keywordQuery.should(QueryBuilders.matchQuery("subject_extended", keyword).boost(3.0f));
        keywordQuery.should(QueryBuilders.matchQuery("article_title", keyword).boost(2.5f));
        keywordQuery.should(QueryBuilders.matchQuery("abstract_text", keyword).boost(2.0f));
        keywordQuery.should(QueryBuilders.matchQuery("keyword", keyword).boost(2.5f));
        keywordQuery.should(QueryBuilders.matchQuery("keyword_plus", keyword).boost(2.0f));
        keywordQuery.should(QueryBuilders.matchQuery("subject_traditional", keyword).boost(1.5f));
        keywordQuery.minimumShouldMatch(1);

        boolQueryBuilder.must(keywordQuery);

        // 添加时间段过滤
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sortdate")
                .from(startDate + "-01-01")
                .to(endDate + "-12-31");
        boolQueryBuilder.filter(rangeQuery);

        // 按年份分类的聚合
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_year")
                .field("pubyear.keyword")
                .size(100)
                .order(Terms.Order.key(true));

        // 创建查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .addAggregation(aggregation)
                .withPageable(PageRequest.of(0, 10000))
                .build();

        // 执行查询
        SearchHits<main2022> searchHits = elasticsearchRestTemplate.search(searchQuery, main2022.class);
        List<main2022> allPapers = searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        // 按年份分组返回结果
        return allPapers.stream()
                .collect(Collectors.groupingBy(
                        paper -> paper.getPubyear() != null ? paper.getPubyear() : "Unknown",
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * 获取热门研究主题
     */
    public Map<String, Long> getTopResearchTopics(String startDate, String endDate, int topN) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (startDate != null && endDate != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sortdate")
                    .from(startDate + "-01-01")
                    .to(endDate + "-12-31");
            boolQueryBuilder.filter(rangeQuery);
        }

        // 按研究领域聚合
        TermsAggregationBuilder topicAggregation = AggregationBuilders.terms("top_topics")
                .field("subject_extended.keyword")
                .size(topN);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .addAggregation(topicAggregation)
                .withPageable(PageRequest.of(0, 0)) // 只需要聚合结果
                .build();

        SearchHits<main2022> searchHits = elasticsearchRestTemplate.search(searchQuery, main2022.class);
        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();

        if (aggregations != null) {
            Terms topicTerms = aggregations.aggregations().get("top_topics");
            return topicTerms.getBuckets().stream()
                    .collect(Collectors.toMap(
                            Terms.Bucket::getKeyAsString,
                            Terms.Bucket::getDocCount,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }

        return new LinkedHashMap<>();
    }

    /**
     * 获取合作网络数据
     */
    public Map<String, Object> getCollaborationNetwork(String keyword, String startDate, String endDate) {
        Map<String, List<main2022>> data = searchByKeywordAndDateRange(keyword, startDate, endDate);

        // 分析国家间合作
        Map<String, Set<String>> countryCollaborations = new HashMap<>();
        Map<String, Integer> institutionCounts = new HashMap<>();

        for (List<main2022> papers : data.values()) {
            for (main2022 paper : papers) {
                if (paper.getAddress() != null) {
                    String[] addresses = paper.getAddress().split(";");
                    Set<String> paperCountries = new HashSet<>();

                    for (String address : addresses) {
                        String country = extractCountryFromAddress(address.trim());
                        if (country != null && !country.isEmpty()) {
                            paperCountries.add(country);
                        }

                        // 提取机构
                        String institution = extractInstitutionFromAddress(address.trim());
                        if (institution != null && !institution.isEmpty()) {
                            institutionCounts.merge(institution, 1, Integer::sum);
                        }
                    }

                    // 记录国家间合作
                    for (String country1 : paperCountries) {
                        for (String country2 : paperCountries) {
                            if (!country1.equals(country2)) {
                                countryCollaborations
                                        .computeIfAbsent(country1, k -> new HashSet<>())
                                        .add(country2);
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("countryCollaborations", countryCollaborations);
        result.put("topInstitutions", institutionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        return result;
    }

    /**
     * 从地址中提取国家信息的辅助方法
     */
    private String extractCountryFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        // 常见国家名称映射
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("USA", "United States");
        countryMappings.put("US", "United States");
        countryMappings.put("UNITED STATES", "United States");
        countryMappings.put("UK", "United Kingdom");
        countryMappings.put("ENGLAND", "United Kingdom");
        countryMappings.put("CHINA", "China");
        countryMappings.put("PEOPLES R CHINA", "China");
        countryMappings.put("GERMANY", "Germany");
        countryMappings.put("JAPAN", "Japan");
        countryMappings.put("FRANCE", "France");
        countryMappings.put("CANADA", "Canada");
        countryMappings.put("AUSTRALIA", "Australia");
        countryMappings.put("ITALY", "Italy");
        countryMappings.put("SPAIN", "Spain");
        countryMappings.put("NETHERLANDS", "Netherlands");
        countryMappings.put("SWITZERLAND", "Switzerland");
        countryMappings.put("SWEDEN", "Sweden");
        countryMappings.put("NORWAY", "Norway");
        countryMappings.put("DENMARK", "Denmark");
        countryMappings.put("FINLAND", "Finland");
        countryMappings.put("BELGIUM", "Belgium");
        countryMappings.put("AUSTRIA", "Austria");
        countryMappings.put("SOUTH KOREA", "South Korea");
        countryMappings.put("KOREA", "South Korea");
        countryMappings.put("INDIA", "India");
        countryMappings.put("BRAZIL", "Brazil");
        countryMappings.put("RUSSIA", "Russia");
        countryMappings.put("ISRAEL", "Israel");
        countryMappings.put("SINGAPORE", "Singapore");

        String[] parts = address.split("[,;]");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1].trim().toUpperCase();

            for (Map.Entry<String, String> entry : countryMappings.entrySet()) {
                if (lastPart.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }

            return cleanCountryName(lastPart);
        }

        return null;
    }

    /**
     * 清理国家名称
     */
    private String cleanCountryName(String countryName) {
        if (countryName == null) return null;

        countryName = countryName.replaceAll("\\d+", "").trim();
        countryName = countryName.replaceAll("[^a-zA-Z\\s]", "").trim();

        return countryName.length() > 2 ? countryName : null;
    }

    /**
     * 从地址中提取机构信息
     */
    private String extractInstitutionFromAddress(String address) {
        if (address == null) return null;

        String[] parts = address.split("[,;]");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().contains("univ") ||
                    trimmed.toLowerCase().contains("institute") ||
                    trimmed.toLowerCase().contains("college") ||
                    trimmed.toLowerCase().contains("hospital") ||
                    trimmed.toLowerCase().contains("school")) {
                return trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
            }
        }
        return null;
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
}ear.keyword")
        .size(100)
                .order(Terms.Order.key(true)); // 按年份排序

// 创建 ElasticSearch 查询
NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(boolQueryBuilder)
        .addAggregation(aggregation)
        .withPageable(PageRequest.of(0, 10000)) // 增加返回结果数量
        .build();

// 执行查询
SearchHits<main2022> searchHits = elasticsearchRestTemplate.search(searchQuery, main2022.class);
List<main2022> allPapers = searchHits.stream()
        .map(hit -> hit.getContent())
        .collect(Collectors.toList());

// 获取聚合结果
ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();

        if (elasticsearchAggregations != null) {
Terms yearTerms = elasticsearchAggregations.aggregations().get("by_year");

// 使用聚合结果创建年份分组
Map<String, List<main2022>> result = new TreeMap<>(); // TreeMap保持年份排序

            for (Terms.Bucket bucket : yearTerms.getBuckets()) {
String year = bucket.getKeyAsString();
List<main2022> yearPapers = allPapers.stream()
        .filter(paper -> year.equals(paper.getPubyear()))
        .collect(Collectors.toList());

                if (!yearPapers.isEmpty()) {
        result.put(year, yearPapers);
                }
                        }

                        return result;
        } else {
                // 如果没有聚合结果，手动按年份分组
                return allPapers.stream()
                    .collect(Collectors.groupingBy(
        paper -> paper.getPubyear() != null ? paper.getPubyear() : "Unknown",
TreeMap::new,
        Collectors.toList()
                    ));
                            }
                            }

/**
 * 增强的搜索方法，支持更多搜索选项
 */
public Map<String, List<main2022>> advancedDisciplinarySearch(String keyword,
                                                              String startDate,
                                                              String endDate,
                                                              List<String> countries,
                                                              List<String> journals,
                                                              String documentType) {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

    // 关键词搜索
    if (keyword != null && !keyword.trim().isEmpty()) {
        BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
        keywordQuery.should(QueryBuilders.matchQuery("subject_extended", keyword).boost(3.0f));
        keywordQuery.should(QueryBuilders.matchQuery("article_title", keyword).boost(2.5f));
        keywordQuery.should(QueryBuilders.matchQuery("abstract_text", keyword).boost(2.0f));
        keywordQuery.should(QueryBuilders.matchQuery("keyword", keyword).boost(2.5f));
        keywordQuery.minimumShouldMatch(1);
        boolQueryBuilder.must(keywordQuery);
    }

    // 时间范围过滤
    if (startDate != null && endDate != null) {
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("sortdate")
                .from(startDate + "-01-01")
                .to(endDate + "-12-31");
        boolQueryBuilder.filter(rangeQuery);
    }

    // 国家过滤
    if (countries != null && !countries.isEmpty()) {
        BoolQueryBuilder countryQuery = QueryBuilders.boolQuery();
        for (String country : countries) {
            countryQuery.should(QueryBuilders.wildcardQuery("reprint_address", "*" + country + "*"));
        }
        boolQueryBuilder.filter(countryQuery);
    }

    // 期刊过滤
    if (journals != null && !journals.isEmpty()) {
        boolQueryBuilder.filter(QueryBuilders.termsQuery("journal_title_source.keyword", journals));
    }

    // 文档类型过滤
    if (documentType != null && !documentType.trim().isEmpty()) {
        boolQueryBuilder.filter(QueryBuilders.matchQuery("article_doctype", documentType));
    }

    // 按年份分类的聚合
    TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_year")
            .field("puby