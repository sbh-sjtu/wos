package com.example.backend.service.impl;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 动态表选择服务 - 增强版
 * 支持学科分析的表选择逻辑
 */
@Service
public class TableSelectorService {

    // 扩展支持的年份范围
    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2020;

    /**
     * 新增：根据关键词和年份范围确定需要查询的表（专用于学科分析）
     */
    public List<String> determineTablesForDisciplinaryAnalysis(String keyword, String startYear, String endYear) {
        try {
            Integer start = startYear != null ? Integer.parseInt(startYear) : MIN_YEAR;
            Integer end = endYear != null ? Integer.parseInt(endYear) : MAX_YEAR;

            return determineTablesByYearRange(start, end);
        } catch (NumberFormatException e) {
            System.err.println("年份格式错误，使用默认范围: " + e.getMessage());
            return getAllSupportedTables();
        }
    }

    /**
     * 新增：验证年份范围是否有效
     */
    public boolean isYearRangeValid(String startYear, String endYear) {
        try {
            int start = Integer.parseInt(startYear);
            int end = Integer.parseInt(endYear);

            return start >= MIN_YEAR && end <= MAX_YEAR && start <= end;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 新增：获取有效的年份范围（调整到支持范围内）
     */
    public int[] getValidYearRange(String startYear, String endYear) {
        try {
            int start = Integer.parseInt(startYear);
            int end = Integer.parseInt(endYear);

            start = Math.max(start, MIN_YEAR);
            end = Math.min(end, MAX_YEAR);

            if (start > end) {
                start = MIN_YEAR;
                end = MAX_YEAR;
            }

            return new int[]{start, end};
        } catch (NumberFormatException e) {
            return new int[]{MIN_YEAR, MAX_YEAR};
        }
    }

    // 保持原有的其他方法不变...
    public List<String> determineTablesFromFilters(List<?> searchFilters) {
        Set<Integer> targetYears = new HashSet<>();

        if (searchFilters != null && !searchFilters.isEmpty()) {
            for (Object filter : searchFilters) {
                Set<Integer> yearsFromFilter = extractYearsFromFilter(filter);
                targetYears.addAll(yearsFromFilter);
            }
        }

        if (targetYears.isEmpty()) {
            return getAllSupportedTables();
        }

        return targetYears.stream()
                .filter(year -> year >= MIN_YEAR && year <= MAX_YEAR)
                .map(year -> "Wos_" + year)
                .collect(Collectors.toList());
    }

    public List<String> determineTablesByYearRange(Integer startYear, Integer endYear) {
        if (startYear == null || endYear == null) {
            return getAllSupportedTables();
        }

        int start = Math.max(startYear, MIN_YEAR);
        int end = Math.min(endYear, MAX_YEAR);

        if (start > end) {
            return new ArrayList<>();
        }

        return IntStream.rangeClosed(start, end)
                .mapToObj(year -> "Wos_" + year)
                .collect(Collectors.toList());
    }

    public List<String> getAllSupportedTables() {
        return IntStream.rangeClosed(MIN_YEAR, MAX_YEAR)
                .mapToObj(year -> "Wos_" + year)
                .collect(Collectors.toList());
    }

    public String getSupportedYearRange() {
        return MIN_YEAR + "-" + MAX_YEAR;
    }

    public boolean isYearSupported(int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }

    // 其他私有方法保持不变...
    private Set<Integer> extractYearsFromFilter(Object filter) {
        Set<Integer> years = new HashSet<>();
        try {
            String input = getInputFromFilter(filter);
            if (input == null || input.trim().isEmpty()) {
                return years;
            }
            Integer searchType = getSearchTypeFromFilter(filter);
            if (searchType != null && searchType == 5) {
                years.addAll(parseYearsFromInput(input));
            }
        } catch (Exception e) {
            System.err.println("提取年份信息失败: " + e.getMessage());
        }
        return years;
    }

    private String getInputFromFilter(Object filter) {
        try {
            return (String) filter.getClass().getMethod("getInput").invoke(filter);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getSearchTypeFromFilter(Object filter) {
        try {
            Object selects = filter.getClass().getMethod("getSelects").invoke(filter);
            if (selects instanceof List) {
                List<?> selectList = (List<?>) selects;
                if (selectList.size() > 1) {
                    return (Integer) selectList.get(1);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Set<Integer> parseYearsFromInput(String input) {
        Set<Integer> years = new HashSet<>();
        if (input == null || input.trim().isEmpty()) {
            return years;
        }
        input = input.trim();
        try {
            if (input.contains("-")) {
                String[] parts = input.split("-");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    for (int year = start; year <= end; year++) {
                        years.add(year);
                    }
                    return years;
                }
            }
            if (input.contains(",")) {
                String[] parts = input.split(",");
                for (String part : parts) {
                    try {
                        int year = Integer.parseInt(part.trim());
                        years.add(year);
                    } catch (NumberFormatException e) {
                        // 忽略无效的年份
                    }
                }
                return years;
            }
            int year = Integer.parseInt(input);
            years.add(year);
        } catch (NumberFormatException e) {
            System.out.println("无法解析年份: " + input);
        }
        return years;
    }
}