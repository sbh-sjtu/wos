package com.example.backend.service;

import com.example.backend.model.main2022;
import java.util.List;
import java.util.Map;

public interface Main2022ElasticSearchService {
    public List<main2022> fullTextSearch(String query);

    public Map<String, List<main2022>> searchByKeywordAndDateRange(String keyword, String startDate, String endDate);
}