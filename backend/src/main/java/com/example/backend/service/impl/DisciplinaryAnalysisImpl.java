package com.example.backend.service.impl;

import com.example.backend.model.main2022;
import com.example.backend.service.DisciplinaryAnalysis;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DisciplinaryAnalysisImpl implements DisciplinaryAnalysis {

    @Override
    public Map<String, Object> analyzeDisciplinaryData(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Object> result = new HashMap<>();

        // 1. 论文数量趋势分析
        Map<String, Integer> yearlyPaperCount = analyzeYearlyTrend(disciplinaryData);
        result.put("yearlyTrend", yearlyPaperCount);

        // 2. 国家分布分析
        Map<String, Integer> countryDistribution = analyzeCountryDistribution(disciplinaryData);
        result.put("countryDistribution", countryDistribution);

        // 3. 顶级作者/机构分析
        Map<String, Object> authorAnalysis = analyzeTopAuthorsAndInstitutions(disciplinaryData);
        result.put("authorAnalysis", authorAnalysis);

        // 4. 期刊分布分析
        Map<String, Integer> journalDistribution = analyzeJournalDistribution(disciplinaryData);
        result.put("journalDistribution", journalDistribution);

        // 5. 关键词趋势分析
        Map<String, Map<String, Integer>> keywordTrends = analyzeKeywordTrends(disciplinaryData);
        result.put("keywordTrends", keywordTrends);

        // 6. 总体统计信息
        Map<String, Object> summary = generateSummaryStatistics(disciplinaryData);
        result.put("summary", summary);

        return result;
    }

    /**
     * 分析论文数量年度趋势
     */
    private Map<String, Integer> analyzeYearlyTrend(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Integer> yearlyCount = new TreeMap<>(); // TreeMap保持年份排序

        for (Map.Entry<String, List<main2022>> entry : disciplinaryData.entrySet()) {
            String year = entry.getKey();
            int count = entry.getValue().size();
            yearlyCount.put(year, count);
        }

        return yearlyCount;
    }

    /**
     * 分析国家分布情况
     */
    private Map<String, Integer> analyzeCountryDistribution(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Integer> countryCount = new HashMap<>();

        for (List<main2022> papers : disciplinaryData.values()) {
            for (main2022 paper : papers) {
                String country = extractCountryFromAddress(paper.getReprint_address());
                if (country != null && !country.isEmpty()) {
                    countryCount.merge(country, 1, Integer::sum);
                }
            }
        }

        // 按数量降序排序，只返回前20个国家
        return countryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 从地址中提取国家信息
     */
    private String extractCountryFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        // 常见国家名称映射
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("USA", "United States");
        countryMappings.put("US", "United States");
        countryMappings.put("UNITED STATES", "United States");
        countryMappings.put("UK", "United Kingdom");
        countryMappings.put("ENGLAND", "United Kingdom");
        countryMappings.put("BRITAIN", "United Kingdom");
        countryMappings.put("CHINA", "China");
        countryMappings.put("PEOPLES R CHINA", "China");
        countryMappings.put("PRC", "China");
        countryMappings.put("GERMANY", "Germany");
        countryMappings.put("JAPAN", "Japan");
        countryMappings.put("FRANCE", "France");
        countryMappings.put("CANADA", "Canada");
        countryMappings.put("AUSTRALIA", "Australia");
        countryMappings.put("ITALY", "Italy");
        countryMappings.put("SPAIN", "Spain");
        countryMappings.put("NETHERLANDS", "Netherlands");
        countryMappings.put("SWITZERLAND", "Switzerland");
        countryMappings.put("SWEDEN", "Sweden");
        countryMappings.put("NORWAY", "Norway");
        countryMappings.put("DENMARK", "Denmark");
        countryMappings.put("FINLAND", "Finland");
        countryMappings.put("BELGIUM", "Belgium");
        countryMappings.put("AUSTRIA", "Austria");
        countryMappings.put("SOUTH KOREA", "South Korea");
        countryMappings.put("KOREA", "South Korea");
        countryMappings.put("INDIA", "India");
        countryMappings.put("BRAZIL", "Brazil");
        countryMappings.put("RUSSIA", "Russia");
        countryMappings.put("ISRAEL", "Israel");
        countryMappings.put("SINGAPORE", "Singapore");

        // 分割地址，通常最后一部分是国家
        String[] parts = address.split("[,;]");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1].trim().toUpperCase();

            // 直接匹配
            if (countryMappings.containsKey(lastPart)) {
                return countryMappings.get(lastPart);
            }

            // 部分匹配
            for (Map.Entry<String, String> entry : countryMappings.entrySet()) {
                if (lastPart.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }

            // 如果没有匹配到，返回原始的最后部分（清理后）
            return cleanCountryName(lastPart);
        }

        return null;
    }

    /**
     * 清理国家名称
     */
    private String cleanCountryName(String countryName) {
        if (countryName == null) return null;

        // 移除邮政编码和其他数字
        countryName = countryName.replaceAll("\\d+", "").trim();
        // 移除特殊字符
        countryName = countryName.replaceAll("[^a-zA-Z\\s]", "").trim();

        return countryName.length() > 2 ? countryName : null;
    }

    /**
     * 分析顶级作者和机构
     */
    private Map<String, Object> analyzeTopAuthorsAndInstitutions(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Integer> authorCount = new HashMap<>();
        Map<String, Integer> institutionCount = new HashMap<>();

        for (List<main2022> papers : disciplinaryData.values()) {
            for (main2022 paper : papers) {
                // 分析作者
                if (paper.getAuthor_fullname() != null) {
                    String[] authors = paper.getAuthor_fullname().split("[;,]");
                    for (String author : authors) {
                        String cleanAuthor = author.trim();
                        if (!cleanAuthor.isEmpty()) {
                            authorCount.merge(cleanAuthor, 1, Integer::sum);
                        }
                    }
                }

                // 分析机构
                if (paper.getAddress() != null) {
                    String[] institutions = extractInstitutions(paper.getAddress());
                    for (String institution : institutions) {
                        if (!institution.isEmpty()) {
                            institutionCount.merge(institution, 1, Integer::sum);
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();

        // 返回前10名作者
        result.put("topAuthors", authorCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        // 返回前10名机构
        result.put("topInstitutions", institutionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        return result;
    }

    /**
     * 从地址中提取机构信息
     */
    private String[] extractInstitutions(String address) {
        if (address == null) return new String[0];

        // 简单的机构提取逻辑，寻找大学、研究所等关键词
        Pattern pattern = Pattern.compile("([^,;]+(?:Univ|University|Institute|Lab|Laboratory|College|Hospital|School)[^,;]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(address);

        Set<String> institutions = new HashSet<>();
        while (matcher.find()) {
            String institution = matcher.group(1).trim();
            if (institution.length() > 5) { // 过滤太短的结果
                institutions.add(institution);
            }
        }

        return institutions.toArray(new String[0]);
    }

    /**
     * 分析期刊分布
     */
    private Map<String, Integer> analyzeJournalDistribution(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Integer> journalCount = new HashMap<>();

        for (List<main2022> papers : disciplinaryData.values()) {
            for (main2022 paper : papers) {
                String journal = paper.getJournal_title_source();
                if (journal != null && !journal.trim().isEmpty()) {
                    journalCount.merge(journal.trim(), 1, Integer::sum);
                }
            }
        }

        // 返回前15个期刊
        return journalCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 分析关键词趋势
     */
    private Map<String, Map<String, Integer>> analyzeKeywordTrends(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Map<String, Integer>> yearlyKeywords = new HashMap<>();

        for (Map.Entry<String, List<main2022>> entry : disciplinaryData.entrySet()) {
            String year = entry.getKey();
            Map<String, Integer> keywordCount = new HashMap<>();

            for (main2022 paper : entry.getValue()) {
                if (paper.getKeyword() != null) {
                    String[] keywords = paper.getKeyword().split("[;,]");
                    for (String keyword : keywords) {
                        String cleanKeyword = keyword.trim().toLowerCase();
                        if (!cleanKeyword.isEmpty() && cleanKeyword.length() > 2) {
                            keywordCount.merge(cleanKeyword, 1, Integer::sum);
                        }
                    }
                }
            }

            // 只保留前10个关键词
            Map<String, Integer> topKeywords = keywordCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            yearlyKeywords.put(year, topKeywords);
        }

        return yearlyKeywords;
    }

    /**
     * 生成总体统计信息
     */
    private Map<String, Object> generateSummaryStatistics(Map<String, List<main2022>> disciplinaryData) {
        Map<String, Object> summary = new HashMap<>();

        int totalPapers = disciplinaryData.values().stream()
                .mapToInt(List::size)
                .sum();

        Set<String> uniqueAuthors = new HashSet<>();
        Set<String> uniqueJournals = new HashSet<>();
        Set<String> uniqueCountries = new HashSet<>();

        for (List<main2022> papers : disciplinaryData.values()) {
            for (main2022 paper : papers) {
                if (paper.getAuthor_fullname() != null) {
                    String[] authors = paper.getAuthor_fullname().split("[;,]");
                    for (String author : authors) {
                        uniqueAuthors.add(author.trim());
                    }
                }

                if (paper.getJournal_title_source() != null) {
                    uniqueJournals.add(paper.getJournal_title_source().trim());
                }

                String country = extractCountryFromAddress(paper.getReprint_address());
                if (country != null) {
                    uniqueCountries.add(country);
                }
            }
        }

        summary.put("totalPapers", totalPapers);
        summary.put("uniqueAuthors", uniqueAuthors.size());
        summary.put("uniqueJournals", uniqueJournals.size());
        summary.put("uniqueCountries", uniqueCountries.size());
        summary.put("yearRange", disciplinaryData.keySet().stream()
                .sorted()
                .collect(Collectors.toList()));

        return summary;
    }
}