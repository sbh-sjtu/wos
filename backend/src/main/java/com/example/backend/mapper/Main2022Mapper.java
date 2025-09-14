package com.example.backend.mapper;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import com.example.backend.provider.SqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Main2022Mapper {

    // ==================== 新增：单表精确查询方法 ====================

    /**
     * 根据WOS_UID查询单条记录（利用主键索引）
     */
    @Select("SELECT * FROM [${tableName}] WHERE wos_uid = #{wosUid}")
    main2022 findByWosUidInTable(@Param("tableName") String tableName,
                                 @Param("wosUid") String wosUid);

    /**
     * 根据标题精确查询
     */
    @Select("SELECT TOP 1 * FROM [${tableName}] WHERE article_title = #{title}")
    main2022 findByTitleExactInTable(@Param("tableName") String tableName,
                                     @Param("title") String title);

    /**
     * 根据标题模糊查询（返回第一条匹配）
     */
    @Select("SELECT TOP 1 * FROM [${tableName}] WHERE article_title LIKE '%' + #{title} + '%'")
    main2022 findByTitleLikeInTable(@Param("tableName") String tableName,
                                    @Param("title") String title);

    /**
     * 批量查询WOS_UID
     */
    @Select({
            "<script>",
            "SELECT * FROM [${tableName}] WHERE wos_uid IN",
            "<foreach collection='wosUids' item='uid' open='(' separator=',' close=')'>",
            "#{uid}",
            "</foreach>",
            "</script>"
    })
    List<main2022> findByWosUidsInTable(@Param("tableName") String tableName,
                                        @Param("wosUids") List<String> wosUids);

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
     * 高级搜索（限制500条）- 单表版本
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