package com.example.backend.controller;

import com.example.backend.config.SearchFilter;
import com.example.backend.config.DisciplinaryRequest;
import com.example.backend.model.main2022;
import com.example.backend.service.DisciplinaryAnalysis;
import com.example.backend.service.Main2022Service;
import com.example.backend.service.Main2022ElasticSearchService;
import com.example.backend.service.impl.Main2022ElasticSearchServiceImpl;
import com.example.backend.service.impl.Main2022ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for main2022（支持动态多表查询）
 */
@RestController
@RequestMapping("/main2022")
@CrossOrigin
public class Main2022Controller {
    private final Main2022Service main2022Service;
    private final Main2022ElasticSearchService main2022ElasticSearchService;
    private final DisciplinaryAnalysis disciplinaryAnalysis;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public Main2022Controller(Main2022Service main2022Service,
                              Main2022ElasticSearchService main2022ElasticSearchService,
                              DisciplinaryAnalysis disciplinaryAnalysis) {
        this.main2022Service = main2022Service;
        this.main2022ElasticSearchService = main2022ElasticSearchService;
        this.disciplinaryAnalysis = disciplinaryAnalysis;
    }

    @PostMapping(value = "/advancedSearch")
    public List<main2022> selectAll(@RequestBody List<SearchFilter> selectInfo) {
        for (SearchFilter searchFilter : selectInfo) {
            System.out.println("id:" + searchFilter.getId());
            System.out.println("selects:" + searchFilter.getSelects());
            System.out.println("input:" + searchFilter.getInput());
        }
        return main2022Service.advancedSearch(selectInfo);
    }

    /**
     * 新增：按年份范围进行高级搜索
     */
    @PostMapping(value = "/advancedSearchByYear")
    public ResponseEntity<Map<String, Object>> advancedSearchByYear(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 解析请求参数
            @SuppressWarnings("unchecked")
            List<SearchFilter> filters = (List<SearchFilter>) requestData.get("filters");
            Integer startYear = (Integer) requestData.get("startYear");
            Integer endYear = (Integer) requestData.get("endYear");

            // 验证参数
            if (filters == null || filters.isEmpty()) {
                response.put("error", "搜索条件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 如果年份为空，使用默认范围
            if (startYear == null || endYear == null) {
                startYear = 1970;
                endYear = 1979;
            }

            // 检查年份范围是否有效
            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            if (!serviceImpl.isYearSupported(startYear) || !serviceImpl.isYearSupported(endYear)) {
                response.put("error", "年份范围超出支持范围: " + serviceImpl.getSupportedYearRange());
                response.put("supportedRange", serviceImpl.getSupportedYearRange());
                return ResponseEntity.badRequest().body(response);
            }

            // 执行搜索
            List<main2022> results = serviceImpl.advancedSearchByYearRange(filters, startYear, endYear);

            response.put("data", results);
            response.put("count", results.size());
            response.put("searchedYearRange", startYear + "-" + endYear);
            response.put("supportedYearRange", serviceImpl.getSupportedYearRange());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("按年份范围搜索失败: " + e.getMessage());
            e.printStackTrace();

            response.put("error", "搜索失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 新增：获取支持的年份范围信息
     */
    @GetMapping("/supportedYearRange")
    public ResponseEntity<Map<String, Object>> getSupportedYearRange() {
        Map<String, Object> response = new HashMap<>();

        try {
            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            String yearRange = serviceImpl.getSupportedYearRange();

            response.put("supportedYearRange", yearRange);
            response.put("minYear", 1970);
            response.put("maxYear", 1979);
            response.put("message", "当前支持的年份范围");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "获取年份范围信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/fullTextSearch")
    public List<main2022> search(@RequestParam String query) {
        return main2022ElasticSearchService.fullTextSearch(query);
    }

    @PostMapping("/disciplinaryAnalysis")
    public ResponseEntity<Map<String, Object>> setDisciplinaryAnalysis(@RequestBody DisciplinaryRequest request) {
        try {
            System.out.println("Disciplinary analysis request received");
            String keyword = request.getKeyword();
            String startDate = request.getStartDate();
            String endDate = request.getEndDate();

            System.out.println("Keyword: " + keyword + ", Start: " + startDate + ", End: " + endDate);

            // 验证输入参数
            if (keyword == null || keyword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "关键词不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (startDate == null || endDate == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "开始和结束年份不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 检查年份范围是否在支持范围内
            try {
                int start = Integer.parseInt(startDate);
                int end = Integer.parseInt(endDate);

                Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
                if (start < 1970 || end > 1979) {
                    Map<String, Object> warningResponse = new HashMap<>();
                    warningResponse.put("warning", "部分年份超出当前支持范围 " + serviceImpl.getSupportedYearRange() +
                            "，将只查询支持范围内的数据");
                    warningResponse.put("supportedRange", serviceImpl.getSupportedYearRange());
                }
            } catch (NumberFormatException e) {
                // 如果年份格式不正确，让Elasticsearch处理
            }

            // 获取按年份分组的数据
            Map<String, List<main2022>> data = main2022ElasticSearchService.searchByKeywordAndDateRange(keyword, startDate, endDate);

            System.out.println("从 Elasticsearch 获取的数据: " + data.size() + " 年份的数据");

            if (data.isEmpty()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("message", "未找到相关数据");
                emptyResponse.put("yearlyTrend", new HashMap<>());
                emptyResponse.put("countryDistribution", new HashMap<>());
                emptyResponse.put("journalDistribution", new HashMap<>());
                emptyResponse.put("authorAnalysis", new HashMap<>());
                emptyResponse.put("keywordTrends", new HashMap<>());
                emptyResponse.put("summary", Map.of(
                        "totalPapers", 0,
                        "uniqueAuthors", 0,
                        "uniqueJournals", 0,
                        "uniqueCountries", 0,
                        "yearRange", new ArrayList<>()
                ));
                return ResponseEntity.ok(emptyResponse);
            }

            // 进行多维度分析
            Map<String, Object> analysisResult = disciplinaryAnalysis.analyzeDisciplinaryData(data);

            System.out.println("分析完成，返回结果");
            return ResponseEntity.ok(analysisResult);

        } catch (Exception e) {
            System.err.println("学科分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "分析过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 调试Elasticsearch连接的方法
     */
    @GetMapping("/debug-es")
    public ResponseEntity<Map<String, Object>> debugElasticsearch() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(main2022.class).exists();
            result.put("indexExists", indexExists);

            if (indexExists) {
                // 获取总文档数
                long totalCount = elasticsearchOperations.count(Query.findAll(), main2022.class);
                result.put("totalDocuments", totalCount);

                // 调用调试方法
                ((Main2022ElasticSearchServiceImpl) main2022ElasticSearchService).debugElasticsearchConnection();

                result.put("message", "调试信息已输出到控制台，索引存在，文档数量: " + totalCount);
                result.put("status", "SUCCESS");
            } else {
                result.put("message", "索引不存在，需要先同步数据");
                result.put("status", "INDEX_NOT_EXISTS");
            }

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 快速检查Elasticsearch状态
     */
    @GetMapping("/es-status")
    public ResponseEntity<Map<String, Object>> checkElasticsearchStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean indexExists = elasticsearchOperations.indexOps(main2022.class).exists();
            result.put("indexExists", indexExists);

            if (indexExists) {
                NativeQuery countQuery = new NativeQueryBuilder().build();
                long totalCount = elasticsearchOperations.count(countQuery, main2022.class);
                result.put("totalDocuments", totalCount);
                result.put("status", totalCount > 0 ? "READY" : "EMPTY");
                result.put("message", totalCount > 0 ?
                        "Elasticsearch准备就绪，包含" + totalCount + "条文档" :
                        "索引存在但为空，需要同步数据");
            } else {
                result.put("status", "NO_INDEX");
                result.put("message", "索引不存在，需要创建并同步数据");
                result.put("totalDocuments", 0);
            }

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("message", "无法连接到Elasticsearch: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}