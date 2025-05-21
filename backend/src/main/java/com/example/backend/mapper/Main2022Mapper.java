package com.example.backend.mapper;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import com.example.backend.provider.SqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Main2022Mapper {
    @SelectProvider(type = SqlProvider.class, method = "advancedSearch")
    List<main2022> advancedSearch(@Param("filters") List<SearchFilter> filters);
}
