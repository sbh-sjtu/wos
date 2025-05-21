package com.example.backend.service;
import com.example.backend.config.SearchFilter;
import com.example.backend.model.main2022;
import java.util.List;
public interface Main2022Service {
    List<main2022> advancedSearch(List<SearchFilter> filters);
}
