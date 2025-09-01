package com.example.backend.controller;

import com.example.backend.config.SearchFilter;
import com.example.backend.config.DisciplinaryRequest;
import com.example.backend.model.main2022;
import com.example.backend.service.DisciplinaryAnalysis;
import com.example.backend.service.Main2022Service;
import com.example.backend.service.Main2022ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for main2022（暂时的取名，因为使用的数据是部分2022年的数据）
 */
@RestController
@RequestMapping("/main2022")
@CrossOrigin
public class Main2022Controller {
    private final Main2022Service main2022Service;
    private final Main2022ElasticSearchService main2022ElasticSearchService;
    private final DisciplinaryAnalysis disciplinaryAnalysis;

    @Autowired
    public Main2022Controller(Main2022Service main2022Service,
                              Main2022ElasticSearchService main2022ElasticSearchService,
                              DisciplinaryAnalysis disciplinaryAnalysis) {
        this.main2022Service = main2022Service;
        this.main2022ElasticSearchService = main2022ElasticSearchService;
        this.disciplinaryAnalysis = disciplinaryAnalysis;
    }

    @PostMapping(value = "/advancedSearch")
    public List<main2022> selectAll(@RequestBody List<SearchFilter> selectInfo) {
        for (SearchFilter searchFilter : selectInfo) {
            System.out.println("id:" + searchFilter.getId());
            System.out.println("selects:" + searchFilter.getSelects());
            System.out.println("input:" + searchFilter.getInput());
        }
        return main2022Service.advancedSearch(selectInfo);
    }

    @PostMapping("/fullTextSearch")
    public List<main2022> search(@RequestParam String query) {
        return main2022ElasticSearchService.fullTextSearch(query);
    }

    @PostMapping("/disciplinaryAnalysis")
    public Map<String, Object> setDisciplinaryAnalysis(@RequestBody DisciplinaryRequest request) {
        System.out.println("Disciplinary analysis request received");
        String keyword = request.getKeyword();
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();

        System.out.println("Keyword: " + keyword + ", Start: " + startDate + ", End: " + endDate);

        // 获取按年份分组的数据
        Map<String, List<main2022>> data = main2022ElasticSearchService.searchByKeywordAndDateRange(keyword, startDate, endDate);

        // 进行多维度分析
        return disciplinaryAnalysis.analyzeDisciplinaryData(data);
    }
}