package com.example.backend.service;

import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import java.util.List;
import java.util.function.BiConsumer;

public interface Main2022Service {
    /**
     * 高级搜索（限制200条）
     */
    List<main2022> advancedSearch(List<SearchFilter> filters);

    /**
     * 高级搜索（获取所有数据，不限制数量）
     */
    List<main2022> advancedSearchAll(List<SearchFilter> filters);

    /**
     * 高级搜索（获取所有数据，支持进度回调）
     */
    List<main2022> advancedSearchAllWithProgress(List<SearchFilter> filters, BiConsumer<Integer, Integer> progressCallback);

    /**
     * 计算符合条件的总数量
     */
    int countAdvancedSearch(List<SearchFilter> filters);
}