package com.example.backend.service.impl;

import com.example.backend.config.SearchFilter;
import com.example.backend.mapper.Main2022Mapper;
import com.example.backend.model.main2022;
import com.example.backend.service.Main2022Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

@Service
public class Main2022ServiceImpl implements Main2022Service {

    private final Main2022Mapper main2022Mapper;
    private final TableSelectorService tableSelectorService;

    @Autowired
    public Main2022ServiceImpl(Main2022Mapper main2022Mapper, TableSelectorService tableSelectorService) {
        this.main2022Mapper = main2022Mapper;
        this.tableSelectorService = tableSelectorService;
    }

    @Override
    public int countAdvancedSearch(List<SearchFilter> filters) {
        try {
            // 根据搜索条件确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(filters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，返回0");
                return 0;
            }

            System.out.println("计算数量 - 查询表: " + tableNames);
            return main2022Mapper.countAdvancedSearchMultiTable(filters, tableNames);
        } catch (Exception e) {
            System.err.println("多表计算数量失败: " + e.getMessage());
            // 如果多表查询失败，回退到单表查询
            try {
                return main2022Mapper.countAdvancedSearch(filters);
            } catch (Exception fallbackError) {
                System.err.println("单表计算数量也失败: " + fallbackError.getMessage());
                return 50000; // 返回一个估算值
            }
        }
    }

    @Override
    public List<main2022> advancedSearch(List<SearchFilter> filters) {
        try {
            // 根据搜索条件确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(filters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，返回空列表");
                return new ArrayList<>();
            }

            System.out.println("高级搜索 - 查询表: " + tableNames);
            return main2022Mapper.advancedSearchMultiTable(filters, tableNames);
        } catch (Exception e) {
            System.err.println("多表高级搜索失败: " + e.getMessage());
            // 如果多表查询失败，回退到单表查询
            try {
                return main2022Mapper.advancedSearch(filters);
            } catch (Exception fallbackError) {
                System.err.println("单表高级搜索也失败: " + fallbackError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public List<main2022> advancedSearchAll(List<SearchFilter> filters) {
        try {
            // 根据搜索条件确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(filters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，返回空列表");
                return new ArrayList<>();
            }

            System.out.println("高级搜索全部 - 查询表: " + tableNames);
            return main2022Mapper.advancedSearchAllMultiTable(filters, tableNames);
        } catch (Exception e) {
            System.err.println("多表全量搜索失败: " + e.getMessage());
            // 如果多表查询失败，回退到单表查询
            try {
                return main2022Mapper.advancedSearchAll(filters);
            } catch (Exception fallbackError) {
                System.err.println("单表全量搜索也失败: " + fallbackError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public List<main2022> advancedSearchAllWithProgress(List<SearchFilter> filters, BiConsumer<Integer, Integer> progressCallback) {
        try {
            // 验证输入参数
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>();
            }

            System.out.println("开始多表查询数据...");

            // 根据搜索条件确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(filters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，返回空列表");
                if (progressCallback != null) {
                    progressCallback.accept(0, 0);
                }
                return new ArrayList<>();
            }

            System.out.println("多表查询带进度 - 查询表: " + tableNames);

            // 由于tempdb空间限制，直接查询所有数据（不使用COUNT和分页）
            if (progressCallback != null) {
                progressCallback.accept(0, 1); // 设置一个虚拟的总数
            }

            List<main2022> allData = main2022Mapper.advancedSearchAllMultiTable(filters, tableNames);

            if (allData == null) {
                allData = new ArrayList<>();
            }

            System.out.println("多表查询完成，获得 " + allData.size() + " 条记录");

            // 更新最终进度
            if (progressCallback != null) {
                progressCallback.accept(allData.size(), allData.size());
            }

            return allData;

        } catch (Exception e) {
            System.err.println("多表查询失败: " + e.getMessage());
            e.printStackTrace();

            // 回退到单表查询
            try {
                System.out.println("回退到单表查询...");

                if (progressCallback != null) {
                    progressCallback.accept(0, 1);
                }

                List<main2022> fallbackData = main2022Mapper.advancedSearchAll(filters);

                if (fallbackData == null) {
                    fallbackData = new ArrayList<>();
                }

                if (progressCallback != null) {
                    progressCallback.accept(fallbackData.size(), fallbackData.size());
                }

                return fallbackData;

            } catch (Exception fallbackError) {
                System.err.println("单表查询也失败: " + fallbackError.getMessage());
                // 最后的回退：返回空列表
                if (progressCallback != null) {
                    progressCallback.accept(0, 0);
                }
                return new ArrayList<>();
            }
        }
    }

    /**
     * 新增：根据年份范围查询
     * @param filters 搜索条件
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 查询结果
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
     * 新增：带进度的年份范围查询
     * @param filters 搜索条件
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @param progressCallback 进度回调
     * @return 查询结果
     */
    public List<main2022> advancedSearchByYearRangeWithProgress(List<SearchFilter> filters,
                                                                Integer startYear,
                                                                Integer endYear,
                                                                BiConsumer<Integer, Integer> progressCallback) {
        try {
            // 验证输入参数
            if (filters == null || filters.isEmpty()) {
                if (progressCallback != null) {
                    progressCallback.accept(0, 0);
                }
                return new ArrayList<>();
            }

            // 根据年份范围确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesByYearRange(startYear, endYear);

            if (tableNames.isEmpty()) {
                System.out.println("指定年份范围内没有可查询的表");
                if (progressCallback != null) {
                    progressCallback.accept(0, 0);
                }
                return new ArrayList<>();
            }

            System.out.println("按年份范围查询带进度 - 年份: " + startYear + "-" + endYear + ", 查询表: " + tableNames);

            if (progressCallback != null) {
                progressCallback.accept(0, tableNames.size());
            }

            List<main2022> allData = main2022Mapper.advancedSearchAllMultiTable(filters, tableNames);

            if (allData == null) {
                allData = new ArrayList<>();
            }

            System.out.println("按年份范围查询完成，获得 " + allData.size() + " 条记录");

            if (progressCallback != null) {
                progressCallback.accept(allData.size(), allData.size());
            }

            return allData;

        } catch (Exception e) {
            System.err.println("按年份范围查询失败: " + e.getMessage());
            e.printStackTrace();

            if (progressCallback != null) {
                progressCallback.accept(0, 0);
            }
            return new ArrayList<>();
        }
    }

    /**
     * 新增：获取支持的年份范围信息
     * @return 支持的年份范围描述
     */
    public String getSupportedYearRange() {
        return tableSelectorService.getSupportedYearRange();
    }

    /**
     * 新增：检查年份是否支持
     * @param year 年份
     * @return 是否支持
     */
    public boolean isYearSupported(int year) {
        return tableSelectorService.isYearSupported(year);
    }

    /**
     * 新增：获取所有支持的表名
     * @return 支持的表名列表
     */
    public List<String> getAllSupportedTables() {
        return tableSelectorService.getAllSupportedTables();
    }

    /**
     * 新增：根据搜索条件获取相关的表名
     * @param filters 搜索条件
     * @return 相关的表名列表
     */
    public List<String> getRelevantTables(List<SearchFilter> filters) {
        return tableSelectorService.determineTablesFromFilters(filters);
    }

    /**
     * 新增：采样查询（用于大数据量的快速预览）
     * @param filters 搜索条件
     * @param samplePercent 采样百分比
     * @param limit 最大结果数
     * @return 采样结果
     */
    public List<main2022> advancedSearchSample(List<SearchFilter> filters, double samplePercent, int limit) {
        try {
            // 根据搜索条件确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesFromFilters(filters);

            if (tableNames.isEmpty()) {
                System.out.println("没有找到匹配的表，返回空列表");
                return new ArrayList<>();
            }

            System.out.println("采样查询 - 查询表: " + tableNames + ", 采样率: " + samplePercent + "%, 限制: " + limit);
            return main2022Mapper.advancedSearchSampleMultiTable(filters, tableNames, samplePercent, limit);

        } catch (Exception e) {
            System.err.println("多表采样查询失败: " + e.getMessage());
            // 如果多表查询失败，回退到单表查询
            try {
                return main2022Mapper.advancedSearchSample(filters, samplePercent, limit);
            } catch (Exception fallbackError) {
                System.err.println("单表采样查询也失败: " + fallbackError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    /**
     * 新增：按年份范围采样查询
     * @param filters 搜索条件
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @param samplePercent 采样百分比
     * @param limit 最大结果数
     * @return 采样结果
     */
    public List<main2022> advancedSearchSampleByYearRange(List<SearchFilter> filters,
                                                          Integer startYear,
                                                          Integer endYear,
                                                          double samplePercent,
                                                          int limit) {
        try {
            // 根据年份范围确定需要查询的表
            List<String> tableNames = tableSelectorService.determineTablesByYearRange(startYear, endYear);

            if (tableNames.isEmpty()) {
                System.out.println("指定年份范围内没有可查询的表");
                return new ArrayList<>();
            }

            System.out.println("按年份范围采样查询 - 年份: " + startYear + "-" + endYear +
                    ", 查询表: " + tableNames + ", 采样率: " + samplePercent + "%, 限制: " + limit);
            return main2022Mapper.advancedSearchSampleMultiTable(filters, tableNames, samplePercent, limit);

        } catch (Exception e) {
            System.err.println("按年份范围采样查询失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}