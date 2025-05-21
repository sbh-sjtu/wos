package com.example.backend.service.impl;

import com.example.backend.model.disciplinaryInfo;
import com.example.backend.model.main2022;
import com.example.backend.service.DisciplinaryAnalysis;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisciplinaryAnalysisImpl implements DisciplinaryAnalysis {

    @Autowired
    private final NLPService nlpService;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    public DisciplinaryAnalysisImpl(NLPService nlpService, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.nlpService = nlpService;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @Override
    public Map<String, Map<String, Long>> analyzeDisciplinaryData(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Map<String, Long>> result = new HashMap<>();

        for (Map.Entry<String, List<main2022>> entry : disciplinaryData.entrySet()) {
            String year = entry.getKey();
            List<main2022> papers = entry.getValue();

            // 获取 seq_temp List:
            List<Integer> seqTempList = papers.stream()
                    .map(main2022::getSeq_temp)
                    .collect(Collectors.toList());

            // 构建 Elasticsearch 查询
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termsQuery("seq_temp", seqTempList))
                    .build();

            // 在disciplinaryInfo index中搜索NLP处理后的关键词
            List<disciplinaryInfo> searchResults = elasticsearchRestTemplate
                    .search(searchQuery, disciplinaryInfo.class)
                    .map(hit -> hit.getContent())
                    .toList();

            // 处理并统计关键词频率
            Map<String, Long> keywordFrequency = searchResults.stream()
                    .flatMap(info -> Arrays.stream(info.getNlp_keyword().split(",")))
                    .map(String::trim)  // Trim spaces around each keyword
                    .collect(Collectors.groupingBy(keyword -> keyword, Collectors.counting()));

            // 将统计结果存入最终结果
            result.put(year, keywordFrequency);
        }
        return result;
    }


//    @Override
//    public Map<String, Map<String, Long>> analyzeDisciplinaryData(Map<String, List<main2022>> disciplinaryData) {
//        // 存储每年的关键词和词组统计结果
//        Map<String, Map<String, Long>> yearlyKeywordFrequency = new HashMap<>();
//
//        // 遍历每一年
//        for (Map.Entry<String, List<main2022>> entry : disciplinaryData.entrySet()) {
//            String year = entry.getKey();
//            List<main2022> papers = entry.getValue();
//
//            // 初始化当年的统计结果
//            Map<String, Long> yearKeywordCounts = yearlyKeywordFrequency.computeIfAbsent(year, k -> new HashMap<>());
//
//            // 动态计算当前年份的 IDF 分数
//            Map<String, Double> idfScores = calculateIDFScores(papers);
//
//            // 对该年的每篇论文进行单独分析
//            for (main2022 paper : papers) {
//                // 对论文进行分词
//                List<String> tokens = nlpService.tokenize(paper.getAbstract_text() + " " + paper.getArticle_title()+" "+paper.getKeyword());
//
//                // 提取二元词组
//                List<String> phrases = extractPhrases(tokens);
//
//                // 合并单词和词组
//                tokens.addAll(phrases);
//
//                // 提取每篇论文中最关键的关键词或词组
//                List<String> topKeywords = extractTopKeywordsByTFIDF(tokens, idfScores, 1);
//
//                // 更新当年的统计结果
//                topKeywords.forEach(term ->
//                        yearKeywordCounts.merge(term, 1L, Long::sum));
//            }
//
//        }
//        return yearlyKeywordFrequency;
//
//    }
//
//    private List<String> extractPhrases(List<String> tokens) {
//        // 提取二元词组
//        return IntStream.range(0, tokens.size() - 1)
//                .mapToObj(i -> tokens.get(i) + " " + tokens.get(i + 1))
//                .collect(Collectors.toList());
//    }
//
//    private List<String> extractTopKeywordsByTFIDF(List<String> tokens, Map<String, Double> idfScores, int topN) {
//        Map<String, Double> tfidfScores = new HashMap<>();
//
//        for (String token : tokens) {
//            double tf = (double) Collections.frequency(tokens, token) / tokens.size();
//            double idf = idfScores.getOrDefault(token, 1.0); // 使用计算的 IDF 值
//            tfidfScores.put(token, tf * idf);
//        }
//
//        return tfidfScores.entrySet().stream()
//                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
//                .limit(topN)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//    }
//
//    private Map<String, Double> calculateIDFScores(List<main2022> papers) {
//        Map<String, Integer> documentFrequency = new HashMap<>();
//        int totalDocuments = papers.size();
//
//        for (main2022 paper : papers) {
//            List<String> tokens = nlpService.tokenize(paper.getAbstract_text() + " " + paper.getArticle_title()+" "+paper.getKeyword());
//            List<String> phrases = extractPhrases(tokens);
//            tokens.addAll(phrases);
//
//            tokens.stream().distinct().forEach(token ->
//                    documentFrequency.merge(token, 1, Integer::sum));
//        }
//
//        Map<String, Double> idfScores = new HashMap<>();
//        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
//            String term = entry.getKey();
//            int docFreq = entry.getValue();
//            double idf = Math.log((double) totalDocuments / (docFreq + 1)) + 1.0;
//            idfScores.put(term, idf);
//        }
//
//        return idfScores;
//    }
}
