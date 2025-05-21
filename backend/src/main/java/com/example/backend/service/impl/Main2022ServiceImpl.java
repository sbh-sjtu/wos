package com.example.backend.service.impl;

import com.example.backend.config.SearchFilter;
import com.example.backend.mapper.Main2022Mapper;
import com.example.backend.model.main2022;
import com.example.backend.service.Main2022ElasticSearchService;
import com.example.backend.service.Main2022Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Main2022ServiceImpl implements Main2022Service {

    private final Main2022Mapper main2022Mapper;
    private final Main2022ElasticSearchServiceImpl main2022ElasticSearchService;

    @Autowired
    public Main2022ServiceImpl(Main2022Mapper main2022MapperIn, Main2022ElasticSearchServiceImpl main2022ElasticSearchServiceIn) {
        main2022Mapper = main2022MapperIn;
        main2022ElasticSearchService = main2022ElasticSearchServiceIn;
    }

    @Override
    public List<main2022> advancedSearch(List<SearchFilter> filters) {
        return main2022Mapper.advancedSearch(filters);
    }

}
