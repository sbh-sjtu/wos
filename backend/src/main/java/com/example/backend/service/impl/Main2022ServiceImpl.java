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

    @Autowired
    public Main2022ServiceImpl(Main2022Mapper main2022Mapper) {
        this.main2022Mapper = main2022Mapper;
    }

    @Override
    public int countAdvancedSearch(List<SearchFilter> filters) {
        try {
            return main2022Mapper.countAdvancedSearch(filters);
        } catch (Exception e) {
            System.err.println("计算数量失败: " + e.getMessage());
            // 如果计数失败，返回一个估算值
            return 50000;
        }
    }

    @Override
    public List<main2022> advancedSearch(List<SearchFilter> filters) {
        return main2022Mapper.advancedSearch(filters);
    }

    @Override
    public List<main2022> advancedSearchAll(List<SearchFilter> filters) {
        return main2022Mapper.advancedSearchAll(filters);
    }

    @Override
    public List<main2022> advancedSearchAllWithProgress(List<SearchFilter> filters, BiConsumer<Integer, Integer> progressCallback) {
        try {
            // 验证输入参数
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>();
            }

            System.out.println("开始查询数据...");

            // 由于tempdb空间限制，直接查询所有数据（不使用COUNT和分页）
            if (progressCallback != null) {
                progressCallback.accept(0, 1); // 设置一个虚拟的总数
            }

            System.out.println("直接查询所有匹配的数据（避免tempdb问题）");
            List<main2022> allData = main2022Mapper.advancedSearchAll(filters);

            if (allData == null) {
                allData = new ArrayList<>();
            }

            System.out.println("查询完成，获得 " + allData.size() + " 条记录");

            // 更新最终进度
            if (progressCallback != null) {
                progressCallback.accept(allData.size(), allData.size());
            }

            return allData;

        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            e.printStackTrace();

            // 最后的回退：返回空列表
            if (progressCallback != null) {
                progressCallback.accept(0, 0);
            }
            return new ArrayList<>();
        }
    }
}