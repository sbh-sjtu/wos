package com.example.backend.service;

import com.example.backend.model.main2022;

import java.util.List;
import java.util.Map;

public interface DisciplinaryAnalysis {
    public Map<String, Map<String, Long>> analyzeDisciplinaryData(Map<String, List<main2022>> disciplinaryData);
}
