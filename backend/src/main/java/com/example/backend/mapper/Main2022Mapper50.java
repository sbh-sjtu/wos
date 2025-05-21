package com.example.backend.mapper;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import com.example.backend.provider.SqlProvider50;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface Main2022Mapper50 {
    @SelectProvider(type = SqlProvider50.class, method = "advancedSearch")
    List<main2022> advancedSearch(@Param("filters") List<SearchFilter> filters);
}
