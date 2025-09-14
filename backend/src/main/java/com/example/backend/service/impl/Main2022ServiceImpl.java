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
    private static final int MIN_YEAR = 1950;
    private static final int MAX_YEAR = 2020;

    @Autowired
    public Main2022ServiceImpl(Main2022Mapper main2022Mapper, TableSelectorService tableSelectorService) {
        this.main2022Mapper = main2022Mapper;
        this.tableSelectorService = tableSelectorService;
    }

    // ==================== 新增：单条记录精确查询方法 ====================

    /**
     * 根据WOS_UID查询文献（从2020往前查询）
     */
    public main2022 findByWosUid(String wosUid) {
        if (wosUid == null || wosUid.trim().isEmpty()) {
            return null;
        }

        System.out.println("开始查询WOS_UID: " + wosUid);
        long startTime = System.currentTimeMillis();
        int tablesSearched = 0;

        // 从最新年份开始查询
        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            tablesSearched++;
            String tableName = "Wos_" + year;

            try {
                long queryStart = System.currentTimeMillis();
                main2022 result = main2022Mapper.findByWosUidInTable(tableName, wosUid);
                long queryTime = System.currentTimeMillis() - queryStart;

                if (result != null) {
                    long totalTime = System.currentTimeMillis() - startTime;
                    System.out.println(String.format(
                            "找到文献 - 表: %s, 查询时间: %dms, 搜索了%d个表, 总耗时: %dms",
                            tableName, queryTime, tablesSearched, totalTime
                    ));
                    return result;
                }
            } catch (Exception e) {
                // 表可能不存在，继续查询下一个
                continue;
            }
        }

        System.out.println("未找到文献，搜索了" + tablesSearched + "个表");
        return null;
    }

    /**
     * 根据DOI查询文献（从2020往前查询）
     */
    public main2022 findByDoi(String doi) {
        if (doi == null || doi.trim().isEmpty()) {
            return null;
        }

        System.out.println("开始查询DOI: " + doi);
        long startTime = System.currentTimeMillis();
        int tablesSearched = 0;

        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            tablesSearched++;
            String tableName = "Wos_" + year;

            try {
                main2022 result = main2022Mapper.findByDoiInTable(tableName, doi);

                if (result != null) {
                    long totalTime = System.currentTimeMillis() - startTime;
                    System.out.println(String.format(
                            "通过DOI找到文献 - 表: %s, 搜索了%d个表, 总耗时: %dms",
                            tableName, tablesSearched, totalTime
                    ));
                    return result;
                }
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("未通过DOI找到文献，搜索了" + tablesSearched + "个表");
        return null;
    }

    /**
     * 根据标题查询文献（从2020往前查询）
     */
    public main2022 findByTitle(String title, boolean exactMatch) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        System.out.println("开始查询标题: " + title + " (精确匹配: " + exactMatch + ")");
        long startTime = System.currentTimeMillis();
        int tablesSearched = 0;

        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            tablesSearched++;
            String tableName = "Wos_" + year;

            try {
                main2022 result = exactMatch
                        ? main2022Mapper.findByTitleExactInTable(tableName, title)
                        : main2022Mapper.findByTitleLikeInTable(tableName, title);

                if (result != null) {
                    long totalTime = System.currentTimeMillis() - startTime;
                    System.out.println(String.format(
                            "通过标题找到文献 - 表: %s, 搜索了%d个表, 总耗时: %dms",
                            tableName, tablesSearched, totalTime
                    ));
                    return result;
                }
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("未通过标题找到文献，搜索了" + tablesSearched + "个表");
        return null;
    }

    /**
     * 批量根据WOS_UID查询文献
     */
    public List<main2022> findByWosUids(List<String> wosUids) {
        if (wosUids == null || wosUids.isEmpty()) {
            return new ArrayList<>();
        }

        List<main2022> results = new ArrayList<>();
        List<String> remainingIds = new ArrayList<>(wosUids);

        for (int year = MAX_YEAR; year >= MIN_YEAR && !remainingIds.isEmpty(); year--) {
            String tableName = "Wos_" + year;

            try {
                List<main2022> found = main2022Mapper.findByWosUidsInTable(tableName, remainingIds);

                if (!found.isEmpty()) {
                    results.addAll(found);

                    // 移除已找到的ID
                    Set<String> foundIds = found.stream()
                            .map(main2022::getWos_uid)
                            .collect(Collectors.toSet());
                    remainingIds = remainingIds.stream()
                            .filter(id -> !foundIds.contains(id))
                            .collect(Collectors.toList());

                    System.out.println("在表 " + tableName + " 中找到 " + found.size() + " 条记录");
                }
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("批量查询完成，找到 " + results.size() + "/" + wosUids.size() + " 条记录");
        return results;
    }

    // ==================== 增强的高级搜索（支持DOI和Title的多表查询） ====================

    @Override
    public List<main2022> advancedSearch(List<SearchFilter> filters) {
        try {
            // 检查是否是DOI或Title搜索且没有指定年份
            if (shouldUseMultiTableSearch(filters)) {
                System.out.println("检测到DOI/Title搜索且无年份指定，使用多表查询");
                return advancedSearchAllTables(filters, 500);
            }

            // 原有逻辑：添加默认年份或使用指定年份
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
            return new ArrayList<>();
        }
    }

    /**
     * 判断是否需要使用多表搜索
     */
    private boolean shouldUseMultiTableSearch(List<SearchFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return false;
        }

        // 检查是否有年份过滤
        boolean hasYearFilter = filters.stream()
                .anyMatch(filter -> filter.getSelects() != null &&
                        filter.getSelects().size() > 1 &&
                        Integer.valueOf(5).equals(filter.getSelects().get(1))); // 5 = Year Published

        // 检查是否是DOI或Title搜索
        boolean hasDoiOrTitleSearch = filters.stream()
                .anyMatch(filter -> filter.getSelects() != null &&
                        filter.getSelects().size() > 1 &&
                        (Integer.valueOf(2).equals(filter.getSelects().get(1)) || // 2 = Title
                                Integer.valueOf(6).equals(filter.getSelects().get(1)))); // 6 = DOI

        // 如果是DOI或Title搜索且没有年份过滤，则使用多表搜索
        return hasDoiOrTitleSearch && !hasYearFilter;
    }

    /**
     * 在所有表中进行高级搜索（从2020往前）
     */
    private List<main2022> advancedSearchAllTables(List<SearchFilter> filters, int limit) {
        List<main2022> allResults = new ArrayList<>();
        int totalFound = 0;

        System.out.println("开始多表顺序搜索（从2020往前）");

        for (int year = MAX_YEAR; year >= MIN_YEAR && totalFound < limit; year--) {
            String tableName = "Wos_" + year;

            try {
                // 使用单表查询
                List<main2022> results = main2022Mapper.advancedSearchMultiTable(
                        filters,
                        List.of(tableName)
                );

                if (!results.isEmpty()) {
                    int toAdd = Math.min(results.size(), limit - totalFound);
                    allResults.addAll(results.subList(0, toAdd));
                    totalFound += toAdd;

                    System.out.println("在表 " + tableName + " 中找到 " + results.size() + " 条记录");
                }
            } catch (Exception e) {
                // 表可能不存在，继续
                continue;
            }
        }

        System.out.println("多表搜索完成，共找到 " + allResults.size() + " 条记录");
        return allResults;
    }

    @Override
    public List<main2022> advancedSearchAll(List<SearchFilter> filters) {
        try {
            // 检查是否是DOI或Title搜索且没有指定年份
            if (shouldUseMultiTableSearch(filters)) {
                System.out.println("检测到DOI/Title搜索且无年份指定，使用全量多表查询");
                return advancedSearchAllTablesNoLimit(filters);
            }

            // 原有逻辑
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            return main2022Mapper.advancedSearchAllMultiTable(processedFilters, tableNames);
        } catch (Exception e) {
            System.err.println("多表全量搜索失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 在所有表中进行高级搜索（无数量限制）
     */
    private List<main2022> advancedSearchAllTablesNoLimit(List<SearchFilter> filters) {
        List<main2022> allResults = new ArrayList<>();

        System.out.println("开始全量多表顺序搜索（从2020往前）");

        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            String tableName = "Wos_" + year;

            try {
                List<main2022> results = main2022Mapper.advancedSearchAllMultiTable(
                        filters,
                        List.of(tableName)
                );

                if (!results.isEmpty()) {
                    allResults.addAll(results);
                    System.out.println("在表 " + tableName + " 中找到 " + results.size() + " 条记录");
                }
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("全量多表搜索完成，共找到 " + allResults.size() + " 条记录");
        return allResults;
    }

    // ==================== 学科分析相关方法 ====================

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

    // ==================== 其他原有方法保持不变 ====================

    @Override
    public int countAdvancedSearch(List<SearchFilter> filters) {
        try {
            // 检查是否需要多表计数
            if (shouldUseMultiTableSearch(filters)) {
                return countAllTables(filters);
            }

            // 原有逻辑
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            return main2022Mapper.countAdvancedSearchMultiTable(processedFilters, tableNames);
        } catch (Exception e) {
            System.err.println("计算数量失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 在所有表中计数
     */
    private int countAllTables(List<SearchFilter> filters) {
        int totalCount = 0;

        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            String tableName = "Wos_" + year;

            try {
                int count = main2022Mapper.countAdvancedSearchMultiTable(
                        filters,
                        List.of(tableName)
                );
                totalCount += count;

                if (count > 0) {
                    System.out.println("表 " + tableName + " 中有 " + count + " 条记录");
                }
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("所有表中共有 " + totalCount + " 条记录");
        return totalCount;
    }

    @Override
    public List<main2022> advancedSearchAllWithProgress(List<SearchFilter> filters, BiConsumer<Integer, Integer> progressCallback) {
        try {
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>();
            }

            System.out.println("开始多表查询数据...");

            // 检查是否需要多表搜索
            if (shouldUseMultiTableSearch(filters)) {
                return advancedSearchAllTablesWithProgress(filters, progressCallback);
            }

            // 原有逻辑
            List<SearchFilter> processedFilters = addDefaultYearIfNeeded(filters);
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(processedFilters);

            if (tableNames.isEmpty()) {
                tableNames = List.of("Wos_" + DEFAULT_YEAR);
            }

            if (progressCallback != null) {
                progressCallback.accept(0, 1);
            }

            List<main2022> allData = main2022Mapper.advancedSearchAllMultiTable(processedFilters, tableNames);

            if (allData == null) {
                allData = new ArrayList<>();
            }

            if (progressCallback != null) {
                progressCallback.accept(allData.size(), allData.size());
            }

            return allData;

        } catch (Exception e) {
            System.err.println("多表查询失败: " + e.getMessage());
            if (progressCallback != null) {
                progressCallback.accept(0, 0);
            }
            return new ArrayList<>();
        }
    }

    /**
     * 在所有表中搜索（带进度回调）
     */
    private List<main2022> advancedSearchAllTablesWithProgress(List<SearchFilter> filters,
                                                               BiConsumer<Integer, Integer> progressCallback) {
        List<main2022> allResults = new ArrayList<>();
        int tablesProcessed = 0;
        int totalTables = MAX_YEAR - MIN_YEAR + 1;

        for (int year = MAX_YEAR; year >= MIN_YEAR; year--) {
            String tableName = "Wos_" + year;
            tablesProcessed++;

            if (progressCallback != null) {
                progressCallback.accept(tablesProcessed, totalTables);
            }

            try {
                List<main2022> results = main2022Mapper.advancedSearchAllMultiTable(
                        filters,
                        List.of(tableName)
                );

                if (!results.isEmpty()) {
                    allResults.addAll(results);
                    System.out.println("在表 " + tableName + " 中找到 " + results.size() + " 条记录");
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(totalTables, totalTables);
        }

        return allResults;
    }

    /**
     * 添加默认年份（如果没有指定）
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
            // 检查是否是DOI或Title搜索
            boolean needsYearFilter = !filters.stream()
                    .anyMatch(filter -> filter.getSelects() != null &&
                            filter.getSelects().size() > 1 &&
                            (Integer.valueOf(2).equals(filter.getSelects().get(1)) || // Title
                                    Integer.valueOf(6).equals(filter.getSelects().get(1)))); // DOI

            if (needsYearFilter) {
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
        }

        return filters;
    }

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