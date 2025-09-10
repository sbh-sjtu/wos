package com.example.backend.service.impl;

import com.example.backend.config.SearchFilter;
import com.example.backend.mapper.Main2022Mapper;
import com.example.backend.model.main2022;
import com.example.backend.service.Main2022Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;

@Service
public class Main2022ServiceImpl implements Main2022Service {

    private final Main2022Mapper main2022Mapper;
    private final TableSelectorService tableSelectorService;

    // 默认年份设置
    private static final int DEFAULT_YEAR = 2020;

    @Autowired
    public Main2022ServiceImpl(Main2022Mapper main2022Mapper, TableSelectorService tableSelectorService) {
        this.main2022Mapper = main2022Mapper;
        this.tableSelectorService = tableSelectorService;
    }

    // ==================== 新增：学科分析专用方法 ====================

    /**
     * 学科分析专用查询：根据关键词和年份范围查询数据并按年份分组
     */
    public Map<String, List<main2022>> disciplinaryAnalysisSearch(String keyword, String startYear, String endYear) {
        try {
            System.out.println("开始学科分析查询 - 关键词: " + keyword + ", 年份范围: " + startYear + "-" + endYear);

            // 验证参数
            if (keyword == null || keyword.trim().isEmpty()) {
                System.err.println("关键词为空");
                return new TreeMap<>();
            }

            // 验证和调整年份范围
            if (!tableSelectorService.isYearRangeValid(startYear, endYear)) {
                int[] validRange = tableSelectorService.getValidYearRange(startYear, endYear);
                startYear = String.valueOf(validRange[0]);
                endYear = String.valueOf(validRange[1]);
                System.out.println("年份范围已调整为: " + startYear + "-" + endYear);
            }

            // 确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesForDisciplinaryAnalysis(keyword, startYear, endYear);

            if (tableNames.isEmpty()) {
                System.err.println("没有找到可查询的表");
                return new TreeMap<>();
            }

            System.out.println("学科分析查询表: " + tableNames);

            // 构建查询条件
            List<SearchFilter> filters = buildDisciplinaryAnalysisFilters(keyword);

            // 执行查询
            List<main2022> allData = main2022Mapper.disciplinaryAnalysisSearchMultiTable(filters, tableNames);

            if (allData == null || allData.isEmpty()) {
                System.out.println("查询结果为空");
                return new TreeMap<>();
            }

            System.out.println("学科分析查询完成，获得 " + allData.size() + " 条记录");

            // 按年份分组
            Map<String, List<main2022>> groupedData = allData.stream()
                    .filter(paper -> paper.getPubyear() != null && !paper.getPubyear().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            paper -> paper.getPubyear(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            System.out.println("按年份分组结果: " + groupedData.keySet());
            groupedData.forEach((year, papers) ->
                    System.out.println("  " + year + ": " + papers.size() + " 篇"));

            return groupedData;

        } catch (Exception e) {
            System.err.println("学科分析查询失败: " + e.getMessage());
            e.printStackTrace();
            return new TreeMap<>();
        }
    }

    /**
     * 构建学科分析的搜索条件
     */
    private List<SearchFilter> buildDisciplinaryAnalysisFilters(String keyword) {
        List<SearchFilter> filters = new ArrayList<>();

        // 创建Topic搜索条件
        SearchFilter topicFilter = new SearchFilter();
        topicFilter.setId(1);
        topicFilter.setSelects(List.of("AND", 1)); // Topic搜索
        topicFilter.setInput(keyword);
        filters.add(topicFilter);

        return filters;
    }

    // ==================== 实现接口方法（增加默认年份处理） ====================

    @Override
    public int countAdvancedSearch(List<SearchFilter> filters) {
        try {
            // 添加默认年份过滤
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，使用默认年份" + DEFAULT_YEAR);
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            System.out.println("计算数量 - 查询表: " + tableNames);
            return main2022Mapper.countAdvancedSearchMultiTable(processedFilters, tableNames);
        } catch (Exception e) {
            System.err.println("多表计算数量失败: " + e.getMessage());
            try {
                List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
                return main2022Mapper.countAdvancedSearch(processedFilters);
            } catch (Exception fallbackError) {
                System.err.println("单表计算数量也失败: " + fallbackError.getMessage());
                return 50000;
            }
        }
    }

    @Override
    public List<main2022> advancedSearch(List<SearchFilter> filters) {
        try {
            // 添加默认年份过滤
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，使用默认年份" + DEFAULT_YEAR);
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            System.out.println("高级搜索 - 查询表: " + tableNames);
            return main2022Mapper.advancedSearchMultiTable(processedFilters, tableNames);
        } catch (Exception e) {
            System.err.println("多表高级搜索失败: " + e.getMessage());
            try {
                List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
                return main2022Mapper.advancedSearch(processedFilters);
            } catch (Exception fallbackError) {
                System.err.println("单表高级搜索也失败: " + fallbackError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public List<main2022> advancedSearchAll(List<SearchFilter> filters) {
        try {
            // 添加默认年份过滤
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，使用默认年份" + DEFAULT_YEAR);
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            System.out.println("高级搜索全部 - 查询表: " + tableNames);
            return main2022Mapper.advancedSearchAllMultiTable(processedFilters, tableNames);
        } catch (Exception e) {
            System.err.println("多表全量搜索失败: " + e.getMessage());
            try {
                List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
                return main2022Mapper.advancedSearchAll(processedFilters);
            } catch (Exception fallbackError) {
                System.err.println("单表全量搜索也失败: " + fallbackError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public List<main2022> advancedSearchAllWithProgress(List<SearchFilter> filters, BiConsumer<Integer, Integer> progressCallback) {
        try {
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>();
            }

            System.out.println("开始多表查询数据...");

            // 添加默认年份过滤
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，使用默认年份" + DEFAULT_YEAR);
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            System.out.println("多表查询带进度 - 查询表: " + tableNames);

            if (progressCallback != null) {
                progressCallback.accept(0, 1);
            }

            List<main2022> allData = main2022Mapper.advancedSearchAllMultiTable(processedFilters, tableNames);

            if (allData == null) {
                allData = new ArrayList<>();
            }

            System.out.println("多表查询完成，获得 " + allData.size() + " 条记录");

            if (progressCallback != null) {
                progressCallback.accept(allData.size(), allData.size());
            }

            return allData;

        } catch (Exception e) {
            System.err.println("多表查询失败: " + e.getMessage());
            e.printStackTrace();

            try {
                System.out.println("回退到单表查询...");

                if (progressCallback != null) {
                    progressCallback.accept(0, 1);
                }

                List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
                List<main2022> fallbackData = main2022Mapper.advancedSearchAll(processedFilters);

                if (fallbackData == null) {
                    fallbackData = new ArrayList<>();
                }

                if (progressCallback != null) {
                    progressCallback.accept(fallbackData.size(), fallbackData.size());
                }

                return fallbackData;

            } catch (Exception fallbackError) {
                System.err.println("单表查询也失败: " + fallbackError.getMessage());
                if (progressCallback != null) {
                    progressCallback.accept(0, 0);
                }
                return new ArrayList<>();
            }
        }
    }

    /**
     * 新增：如果搜索条件中没有年份过滤，则添加默认年份
     */
    private List<SearchFilter> addDefaultYearIfNeeded(List<SearchFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return filters;
        }

        // 检查是否已有年份搜索条件
        boolean hasYearFilter = filters.stream()
                .anyMatch(filter -> filter.getSelects() != null &&
                        filter.getSelects().size() > 1 &&
                        Integer.valueOf(5).equals(filter.getSelects().get(1))); // 5 = Year Published

        if (!hasYearFilter) {
            System.out.println("未指定年份条件，添加默认年份: " + DEFAULT_YEAR);

            // 创建新的过滤器列表，包含默认年份
            List<SearchFilter> newFilters = new ArrayList<>(filters);

            SearchFilter yearFilter = new SearchFilter();
            yearFilter.setId(newFilters.size() + 1);
            yearFilter.setSelects(List.of("AND", 5)); // Year Published
            yearFilter.setInput(String.valueOf(DEFAULT_YEAR));

            newFilters.add(yearFilter);
            return newFilters;
        }

        return filters;
    }

    // ==================== 扩展方法（原有方法保持） ====================

    /**
     * 根据年份范围查询
     */
    public List<main2022> advancedSearchByYearRange(List<SearchFilter> filters, Integer startYear, Integer endYear) {
        try {
            // 根据年份范围确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesByYearRange(startYear, endYear);

            if (tableNames.isEmpty()) {
                System.out.println("指定年份范围内没有可查询的表");
                return new ArrayList<>();
            }

            System.out.println("按年份范围查询 - 年份: " + startYear + "-" + endYear + ", 查询表: " + tableNames);
            return main2022Mapper.advancedSearchAllMultiTable(filters, tableNames);

        } catch (Exception e) {
            System.err.println("按年份范围查询失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取支持的年份范围信息
     */
    public String getSupportedYearRange() {
        return tableSelectorService.getSupportedYearRange();
    }

    /**
     * 检查年份是否支持
     */
    public boolean isYearSupported(int year) {
        return tableSelectorService.isYearSupported(year);
    }

    /**
     * 获取所有支持的表名
     */
    public List<String> getAllSupportedTables() {
        return tableSelectorService.getAllSupportedTables();
    }

    /**
     * 获取默认年份
     */
    public int getDefaultYear() {
        return DEFAULT_YEAR;
    }
}