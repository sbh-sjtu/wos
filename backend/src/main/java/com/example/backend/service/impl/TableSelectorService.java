package com.example.backend.service.impl;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 动态表选择服务
 * 根据查询条件智能选择需要查询的表
 */
@Service
public class TableSelectorService {

    // 当前支持的年份范围（已经修改过的表）
    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 1979;

    /**
     * 根据搜索条件确定需要查询的表名列表
     * @param searchFilters 搜索条件
     * @return 需要查询的表名列表
     */
    public List<String> determineTablesFromFilters(List<?> searchFilters) {
        Set<Integer> targetYears = new HashSet<>();

        // 从搜索条件中提取年份信息
        if (searchFilters != null && !searchFilters.isEmpty()) {
            for (Object filter : searchFilters) {
                Set<Integer> yearsFromFilter = extractYearsFromFilter(filter);
                targetYears.addAll(yearsFromFilter);
            }
        }

        // 如果没有找到年份信息，查询所有支持的表
        if (targetYears.isEmpty()) {
            return getAllSupportedTables();
        }

        // 过滤出支持的年份并生成表名
        return targetYears.stream()
                .filter(year -> year >= MIN_YEAR && year <= MAX_YEAR)
                .map(year -> "Wos_" + year)
                .collect(Collectors.toList());
    }

    /**
     * 根据年份范围确定需要查询的表
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 表名列表
     */
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

    /**
     * 获取所有支持的表名
     * @return 所有支持的表名列表
     */
    public List<String> getAllSupportedTables() {
        return IntStream.rangeClosed(MIN_YEAR, MAX_YEAR)
                .mapToObj(year -> "Wos_" + year)
                .collect(Collectors.toList());
    }

    /**
     * 检查表是否被支持
     * @param tableName 表名
     * @return 是否支持
     */
    public boolean isTableSupported(String tableName) {
        if (tableName == null || !tableName.startsWith("Wos_")) {
            return false;
        }

        try {
            int year = Integer.parseInt(tableName.substring(4));
            return year >= MIN_YEAR && year <= MAX_YEAR;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 从搜索条件中提取年份信息
     * @param filter 搜索条件
     * @return 年份集合
     */
    private Set<Integer> extractYearsFromFilter(Object filter) {
        Set<Integer> years = new HashSet<>();

        try {
            // 使用反射获取filter的input字段
            String input = getInputFromFilter(filter);
            if (input == null || input.trim().isEmpty()) {
                return years;
            }

            // 获取搜索类型
            Integer searchType = getSearchTypeFromFilter(filter);

            // 如果是年份搜索（类型5）
            if (searchType != null && searchType == 5) {
                years.addAll(parseYearsFromInput(input));
            }

        } catch (Exception e) {
            System.err.println("提取年份信息失败: " + e.getMessage());
        }

        return years;
    }

    /**
     * 从filter对象中获取input字段值
     */
    private String getInputFromFilter(Object filter) {
        try {
            return (String) filter.getClass().getMethod("getInput").invoke(filter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从filter对象中获取搜索类型
     */
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

    /**
     * 从输入字符串中解析年份
     * 支持多种格式：2020, 2020-2022, 2020,2021,2022等
     */
    private Set<Integer> parseYearsFromInput(String input) {
        Set<Integer> years = new HashSet<>();

        if (input == null || input.trim().isEmpty()) {
            return years;
        }

        input = input.trim();

        try {
            // 处理范围格式：2020-2022
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

            // 处理逗号分隔格式：2020,2021,2022
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

            // 处理单个年份
            int year = Integer.parseInt(input);
            years.add(year);

        } catch (NumberFormatException e) {
            // 如果解析失败，返回空集合
            System.out.println("无法解析年份: " + input);
        }

        return years;
    }

    /**
     * 获取支持的年份范围信息
     * @return 年份范围描述
     */
    public String getSupportedYearRange() {
        return MIN_YEAR + "-" + MAX_YEAR;
    }

    /**
     * 检查年份是否在支持范围内
     * @param year 年份
     * @return 是否支持
     */
    public boolean isYearSupported(int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }
}