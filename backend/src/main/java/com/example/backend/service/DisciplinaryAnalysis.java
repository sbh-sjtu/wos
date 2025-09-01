package com.example.backend.service;

import com.example.backend.model.main2022;

import java.util.List;
import java.util.Map;

public interface DisciplinaryAnalysis {
    /**
     * 分析学科数据
     * @param disciplinaryData 按年份分组的论文数据
     * @return 包含多维度分析结果的Map
     */
    Map<String, Object> analyzeDisciplinaryData(Map<String, List<main2022>> disciplinaryData);
}