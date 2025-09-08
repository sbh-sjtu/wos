package com.example.backend.mapper;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import com.example.backend.provider.SqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Main2022Mapper {

    /**
     * 高级搜索（限制500条）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearch")
    List<main2022> advancedSearch(@Param("filters") List<SearchFilter> filters);

    /**
     * 高级搜索（获取所有数据，不限制数量）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchAll")
    List<main2022> advancedSearchAll(@Param("filters") List<SearchFilter> filters);

    /**
     * 计算符合条件的总数量
     */
    @SelectProvider(type = SqlProvider.class, method = "countAdvancedSearch")
    int countAdvancedSearch(@Param("filters") List<SearchFilter> filters);

    /**
     * 采样查询（避免tempdb问题）
     */
    @SelectProvider(type = SqlProvider.class, method = "advancedSearchSample")
    List<main2022> advancedSearchSample(@Param("filters") List<SearchFilter> filters,
                                        @Param("samplePercent") double samplePercent,
                                        @Param("limit") int limit);
}