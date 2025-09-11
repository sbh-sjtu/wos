package com.example.backend.mapper;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import com.example.backend.provider.SqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Main2022Mapper {

    // ==================== 新增：学科分析专用查询方法 ====================

    /**
     * 学科分析专用多表查询
     */
    @SelectProvider(type = SqlProvider.class, method = "disciplinaryAnalysisSearchMultiTable")
    List<main2022> disciplinaryAnalysisSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                                        @Param("tableNames") List<String> tableNames);

    // ==================== 保持原有的所有方法不变 ====================

    /**
     * 动态多表高级搜索（限制500条）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchMultiTable")
    List<main2022> advancedSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                            @Param("tableNames") List<String> tableNames);

    /**
     * 动态多表高级搜索（获取所有数据，不限制数量）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchAllMultiTable")
    List<main2022> advancedSearchAllMultiTable(@Param("filters") List<SearchFilter> filters,
                                               @Param("tableNames") List<String> tableNames);

    /**
     * 动态多表计算符合条件的总数量
     */
    @SelectProvider(type = SqlProvider.class, method = "countAdvancedSearchMultiTable")
    int countAdvancedSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                      @Param("tableNames") List<String> tableNames);

    /**
     * 动态多表采样查询（避免tempdb问题）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchSampleMultiTable")
    List<main2022> advancedSearchSampleMultiTable(@Param("filters") List<SearchFilter> filters,
                                                  @Param("tableNames") List<String> tableNames,
                                                  @Param("samplePercent") double samplePercent,
                                                  @Param("limit") int limit);

    // ==================== 保留原有方法（向后兼容） ====================

    /**
     * 高级搜索（限制200条）- 单表版本
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearch")
    List<main2022> advancedSearch(@Param("filters") List<SearchFilter> filters);

    /**
     * 高级搜索（获取所有数据，不限制数量）- 单表版本
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchAll")
    List<main2022> advancedSearchAll(@Param("filters") List<SearchFilter> filters);

    /**
     * 计算符合条件的总数量 - 单表版本
     */
    @SelectProvider(type = SqlProvider.class, method = "countAdvancedSearch")
    int countAdvancedSearch(@Param("filters") List<SearchFilter> filters);

    /**
     * 采样查询（避免tempdb问题）- 单表版本
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchSample")
    List<main2022> advancedSearchSample(@Param("filters") List<SearchFilter> filters,
                                        @Param("samplePercent") double samplePercent,
                                        @Param("limit") int limit);
}