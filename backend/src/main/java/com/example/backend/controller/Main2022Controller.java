package com.example.backend.controller;

import com.example.backend.config.SearchFilter;
import com.example.backend.config.DisciplinaryRequest;
import com.example.backend.model.main2022;
import com.example.backend.service.DisciplinaryAnalysis;
import com.example.backend.service.Main2022Service;
import com.example.backend.service.impl.Main2022ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for main2022（支持动态多表查询）
 * 增强版：支持WOS_UID、Title的单条查询
 * 已移除DOI查询功能
 */
@RestController
@RequestMapping("/main2022")
@CrossOrigin
public class Main2022Controller {
    private final Main2022Service main2022Service;
    private final DisciplinaryAnalysis disciplinaryAnalysis;

    @Autowired
    public Main2022Controller(Main2022Service main2022Service,
                              DisciplinaryAnalysis disciplinaryAnalysis) {
        this.main2022Service = main2022Service;
        this.disciplinaryAnalysis = disciplinaryAnalysis;
    }

    // ==================== 新增：单条记录查询接口 ====================

    /**
     * 根据WOS_UID获取文献详情
     * URL格式: /main2022/detail/{wosUid}
     * 例如: /main2022/detail/WOS:000123456789
     */
    @GetMapping("/detail/{wosUid}")
    public ResponseEntity<Map<String, Object>> getPaperDetailByWosUid(@PathVariable String wosUid) {
        Map<String, Object> response = new HashMap<>();

        try {
            // URL解码（处理特殊字符如冒号）
            String decodedWosUid = URLDecoder.decode(wosUid, "UTF-8");
            System.out.println("查询WOS_UID: " + decodedWosUid);

            // 记录查询时间
            long startTime = System.currentTimeMillis();

            // 调用Service层查询
            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            main2022 paper = serviceImpl.findByWosUid(decodedWosUid);

            long queryTime = System.currentTimeMillis() - startTime;

            if (paper != null) {
                response.put("success", true);
                response.put("data", paper);
                response.put("queryTime", queryTime + "ms");
                response.put("message", "查询成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "未找到WOS_UID为 " + decodedWosUid + " 的文献");
                response.put("data", null);
                response.put("queryTime", queryTime + "ms");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            System.err.println("查询文献详情失败: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("error", "查询失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据标题获取文献详情
     * URL格式: /main2022/detail/title?value={title}&exact={true/false}
     */
    @GetMapping("/detail/title")
    public ResponseEntity<Map<String, Object>> getPaperDetailByTitle(
            @RequestParam String value,
            @RequestParam(defaultValue = "false") boolean exact) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("查询标题: " + value + " (精确匹配: " + exact + ")");

            long startTime = System.currentTimeMillis();

            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            main2022 paper = serviceImpl.findByTitle(value, exact);

            long queryTime = System.currentTimeMillis() - startTime;

            if (paper != null) {
                response.put("success", true);
                response.put("data", paper);
                response.put("queryTime", queryTime + "ms");
                response.put("message", "通过标题查询成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "未找到标题" + (exact ? "等于" : "包含") + " \"" + value + "\" 的文献");
                response.put("data", null);
                response.put("queryTime", queryTime + "ms");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            System.err.println("通过标题查询失败: " + e.getMessage());

            response.put("success", false);
            response.put("error", "查询失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批量获取文献详情
     * POST /main2022/details/batch
     * Body: ["WOS:001", "WOS:002", ...]
     */
    @PostMapping("/details/batch")
    public ResponseEntity<Map<String, Object>> getPaperDetailsBatch(@RequestBody List<String> wosUids) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (wosUids == null || wosUids.isEmpty()) {
                response.put("success", false);
                response.put("message", "WOS_UID列表不能为空");
                response.put("data", new ArrayList<>());
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("批量查询 " + wosUids.size() + " 个WOS_UID");

            long startTime = System.currentTimeMillis();

            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            List<main2022> papers = serviceImpl.findByWosUids(wosUids);

            long queryTime = System.currentTimeMillis() - startTime;

            response.put("success", true);
            response.put("data", papers);
            response.put("found", papers.size());
            response.put("requested", wosUids.size());
            response.put("queryTime", queryTime + "ms");
            response.put("message", "找到 " + papers.size() + "/" + wosUids.size() + " 条记录");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("批量查询失败: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("error", "批量查询失败: " + e.getMessage());
            response.put("data", new ArrayList<>());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 原有接口保持不变 ====================

    /**
     * 高级搜索接口（限制500条）
     * 现在支持Title的多表查询（已移除DOI查询功能）
     */
    @PostMapping(value = "/advancedSearch")
    public List<main2022> selectAll(@RequestBody List<SearchFilter> selectInfo) {
        for (SearchFilter searchFilter : selectInfo) {
            System.out.println("搜索条件 - id:" + searchFilter.getId() +
                    ", 字段:" + searchFilter.getSelects() +
                    ", 关键词:" + searchFilter.getInput());
        }
        return main2022Service.advancedSearch(selectInfo);
    }

    /**
     * 按年份范围进行高级搜索
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
                startYear = 2020;
                endYear = 2020;
                System.out.println("使用默认年份范围: " + startYear + "-" + endYear);
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
     * 获取支持的年份范围信息
     */
    @GetMapping("/supportedYearRange")
    public ResponseEntity<Map<String, Object>> getSupportedYearRange() {
        Map<String, Object> response = new HashMap<>();

        try {
            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            String yearRange = serviceImpl.getSupportedYearRange();

            response.put("supportedYearRange", yearRange);
            response.put("minYear", 1950);
            response.put("maxYear", 2020);
            response.put("defaultYear", 2020);
            response.put("message", "当前支持的年份范围，未指定年份时默认使用2020年");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "获取年份范围信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 学科分析接口
     * 使用数据库查询，已移除Elasticsearch依赖
     */
    @PostMapping("/disciplinaryAnalysis")
    public ResponseEntity<Map<String, Object>> setDisciplinaryAnalysis(@RequestBody DisciplinaryRequest request) {
        try {
            System.out.println("学科分析请求 - 关键词: " + request.getKeyword() +
                    ", 年份范围: " + request.getStartDate() + "-" + request.getEndDate());

            String keyword = request.getKeyword();
            String startDate = request.getStartDate();
            String endDate = request.getEndDate();

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

                // 检查是否超出支持范围
                if (start < 1950 || end > 2020) {
                    Map<String, Object> warningResponse = new HashMap<>();
                    warningResponse.put("warning", "部分年份超出当前支持范围 " + serviceImpl.getSupportedYearRange() +
                            "，将只查询支持范围内的数据");
                    warningResponse.put("supportedRange", serviceImpl.getSupportedYearRange());

                    // 调整年份范围到支持范围内
                    start = Math.max(start, 1950);
                    end = Math.min(end, 2020);
                    startDate = String.valueOf(start);
                    endDate = String.valueOf(end);

                    System.out.println("年份范围已调整为: " + startDate + "-" + endDate);
                }

                if (start > end) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "开始年份不能大于结束年份");
                    return ResponseEntity.badRequest().body(errorResponse);
                }

            } catch (NumberFormatException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "年份格式不正确，请输入有效的年份");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 使用数据库查询进行学科分析
            System.out.println("开始从数据库查询学科分析数据...");

            Main2022ServiceImpl serviceImpl = (Main2022ServiceImpl) main2022Service;
            Map<String, List<main2022>> data = serviceImpl.disciplinaryAnalysisSearch(keyword, startDate, endDate);

            System.out.println("从数据库获取的数据: " + data.size() + " 年份的数据");

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

            System.out.println("学科分析完成，返回结果");
            return ResponseEntity.ok(analysisResult);

        } catch (Exception e) {
            System.err.println("学科分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "分析过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}