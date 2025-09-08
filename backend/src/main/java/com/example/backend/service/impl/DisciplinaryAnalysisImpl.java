package com.example.backend.service.impl;

import com.example.backend.service.DisciplinaryAnalysis;
import org.springframework.stereotype.Service;

import com.example.backend.model.main2022;

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
                // 优先从address中提取，其次从reprint_address
                Set<String> countries = new HashSet<>();

                if (paper.getAddress() != null && !paper.getAddress().trim().isEmpty()) {
                    countries.addAll(extractCountriesFromAddress(paper.getAddress()));
                }

                if (paper.getReprint_address() != null && !paper.getReprint_address().trim().isEmpty()) {
                    countries.addAll(extractCountriesFromAddress(paper.getReprint_address()));
                }

                for (String country : countries) {
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
     * 从地址中提取国家信息（支持多个国家）
     */
    private Set<String> extractCountriesFromAddress(String address) {
        Set<String> countries = new HashSet<>();
        if (address == null || address.trim().isEmpty()) {
            return countries;
        }

        // 常见国家名称映射
        Map<String, String> countryMappings = getCountryMappings();

        // 分割地址，可能包含多个机构
        String[] parts = address.split(";");
        for (String part : parts) {
            // 通常国家在最后，用逗号分隔
            String[] subParts = part.split(",");
            if (subParts.length > 0) {
                String lastPart = subParts[subParts.length - 1].trim().toUpperCase();

                // 清理并标准化国家名称
                String country = matchCountry(lastPart, countryMappings);
                if (country != null && !country.isEmpty()) {
                    countries.add(country);
                }
            }
        }

        return countries;
    }

    /**
     * 匹配并标准化国家名称
     */
    private String matchCountry(String text, Map<String, String> countryMappings) {
        if (text == null || text.isEmpty()) return null;

        // 移除方括号内容
        text = text.replaceAll("\\[.*?\\]", "").trim();
        // 移除邮政编码
        text = text.replaceAll("\\d{5,}", "").trim();
        // 移除多余的符号
        text = text.replaceAll("[\\[\\]()]", "").trim();

        // 直接匹配
        if (countryMappings.containsKey(text)) {
            return countryMappings.get(text);
        }

        // 部分匹配
        for (Map.Entry<String, String> entry : countryMappings.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 如果没有匹配到但看起来像国家名称，返回清理后的版本
        String cleaned = cleanCountryName(text);
        if (cleaned != null && cleaned.length() > 2 && !cleaned.matches(".*\\d.*")) {
            // 首字母大写
            return Arrays.stream(cleaned.toLowerCase().split("\\s+"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
        }

        return null;
    }

    /**
     * 获取国家映射表
     */
    private Map<String, String> getCountryMappings() {
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("USA", "United States");
        countryMappings.put("US", "United States");
        countryMappings.put("UNITED STATES", "United States");
        countryMappings.put("UK", "United Kingdom");
        countryMappings.put("ENGLAND", "United Kingdom");
        countryMappings.put("BRITAIN", "United Kingdom");
        countryMappings.put("SCOTLAND", "United Kingdom");
        countryMappings.put("WALES", "United Kingdom");
        countryMappings.put("CHINA", "China");
        countryMappings.put("PEOPLES R CHINA", "China");
        countryMappings.put("P R CHINA", "China");
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
        countryMappings.put("REPUBLIC OF KOREA", "South Korea");
        countryMappings.put("INDIA", "India");
        countryMappings.put("BRAZIL", "Brazil");
        countryMappings.put("RUSSIA", "Russia");
        countryMappings.put("RUSSIAN FEDERATION", "Russia");
        countryMappings.put("ISRAEL", "Israel");
        countryMappings.put("SINGAPORE", "Singapore");
        countryMappings.put("SAUDI ARABIA", "Saudi Arabia");
        countryMappings.put("UAE", "United Arab Emirates");
        countryMappings.put("UNITED ARAB EMIRATES", "United Arab Emirates");
        countryMappings.put("MEXICO", "Mexico");
        countryMappings.put("ARGENTINA", "Argentina");
        countryMappings.put("CHILE", "Chile");
        countryMappings.put("POLAND", "Poland");
        countryMappings.put("CZECH REPUBLIC", "Czech Republic");
        countryMappings.put("HUNGARY", "Hungary");
        countryMappings.put("PORTUGAL", "Portugal");
        countryMappings.put("GREECE", "Greece");
        countryMappings.put("TURKEY", "Turkey");
        countryMappings.put("EGYPT", "Egypt");
        countryMappings.put("SOUTH AFRICA", "South Africa");
        countryMappings.put("NEW ZEALAND", "New Zealand");
        countryMappings.put("IRELAND", "Ireland");
        countryMappings.put("MALAYSIA", "Malaysia");
        countryMappings.put("THAILAND", "Thailand");
        countryMappings.put("INDONESIA", "Indonesia");
        countryMappings.put("PHILIPPINES", "Philippines");
        countryMappings.put("VIETNAM", "Vietnam");
        countryMappings.put("PAKISTAN", "Pakistan");
        countryMappings.put("IRAN", "Iran");
        countryMappings.put("IRAQ", "Iraq");
        countryMappings.put("JORDAN", "Jordan");
        countryMappings.put("LEBANON", "Lebanon");
        countryMappings.put("QATAR", "Qatar");
        countryMappings.put("KUWAIT", "Kuwait");
        countryMappings.put("OMAN", "Oman");
        countryMappings.put("BAHRAIN", "Bahrain");
        return countryMappings;
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
                // 分析作者 - 改进的解析逻辑
                if (paper.getAuthor_fullname() != null && !paper.getAuthor_fullname().trim().isEmpty()) {
                    Set<String> authors = parseAuthors(paper.getAuthor_fullname());
                    for (String author : authors) {
                        if (!author.isEmpty() && author.length() > 2) {
                            authorCount.merge(author, 1, Integer::sum);
                        }
                    }
                }

                // 分析机构 - 改进的解析逻辑
                // 首先尝试从address字段提取
                if (paper.getAddress() != null && !paper.getAddress().trim().isEmpty()) {
                    Set<String> institutions = extractInstitutionsImproved(paper.getAddress());
                    for (String institution : institutions) {
                        if (!institution.isEmpty()) {
                            institutionCount.merge(institution, 1, Integer::sum);
                        }
                    }
                }

                // 如果address没有找到机构，尝试从reprint_address提取
                if (institutionCount.isEmpty() && paper.getReprint_address() != null && !paper.getReprint_address().trim().isEmpty()) {
                    Set<String> institutions = extractInstitutionsImproved(paper.getReprint_address());
                    for (String institution : institutions) {
                        if (!institution.isEmpty()) {
                            institutionCount.merge(institution, 1, Integer::sum);
                        }
                    }
                }
            }
        }

        // 过滤掉明显不是机构的条目（比如只包含人名的）
        Map<String, Integer> filteredInstitutions = new HashMap<>();
        for (Map.Entry<String, Integer> entry : institutionCount.entrySet()) {
            String inst = entry.getKey();
            // 确保机构名称包含机构关键词，且不是以方括号开头（作者标记）
            if (!inst.startsWith("[") && containsInstitutionKeyword(inst)) {
                filteredInstitutions.put(inst, entry.getValue());
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

        // 返回前10名机构（使用过滤后的机构列表）
        result.put("topInstitutions", filteredInstitutions.entrySet().stream()
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
     * 改进的作者解析方法
     */
    private Set<String> parseAuthors(String authorString) {
        Set<String> authors = new HashSet<>();

        if (authorString == null || authorString.trim().isEmpty()) {
            return authors;
        }

        // 首先移除方括号及其内容（可能是标记）
        String cleaned = authorString.replaceAll("\\[.*?\\]", "");

        // 使用分号或逗号分割（根据格式判断）
        String[] potentialAuthors;
        if (cleaned.contains(";")) {
            // 分号分隔格式
            potentialAuthors = cleaned.split(";");
        } else {
            // 逗号分隔格式，但要小心处理姓名中的逗号
            // 通常格式是 "LastName, FirstName"
            potentialAuthors = smartSplitAuthors(cleaned);
        }

        for (String author : potentialAuthors) {
            String trimmed = author.trim();

            // 清理作者名称
            trimmed = cleanAuthorName(trimmed);

            if (trimmed.length() > 2 && !trimmed.matches(".*\\d.*")) {
                // 标准化格式：如果是 "LastName, FirstName" 格式，转换为 "FirstName LastName"
                if (trimmed.contains(",")) {
                    String[] parts = trimmed.split(",", 2);
                    if (parts.length == 2) {
                        trimmed = parts[1].trim() + " " + parts[0].trim();
                    }
                }
                authors.add(trimmed);
            }
        }

        return authors;
    }

    /**
     * 智能分割作者名称（处理逗号分隔的情况）
     */
    private String[] smartSplitAuthors(String authorString) {
        List<String> authors = new ArrayList<>();

        // 简单的启发式方法：假设连续的大写字母开头的词组成一个名字
        String[] words = authorString.split(",");
        StringBuilder currentAuthor = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();

            if (word.isEmpty()) continue;

            // 检查是否是新作者的开始
            if (currentAuthor.length() > 0 && isLikelyNewAuthor(word)) {
                authors.add(currentAuthor.toString().trim());
                currentAuthor = new StringBuilder(word);
            } else {
                if (currentAuthor.length() > 0) {
                    currentAuthor.append(", ");
                }
                currentAuthor.append(word);
            }
        }

        if (currentAuthor.length() > 0) {
            authors.add(currentAuthor.toString().trim());
        }

        return authors.toArray(new String[0]);
    }

    /**
     * 判断是否可能是新作者的开始
     */
    private boolean isLikelyNewAuthor(String text) {
        // 如果文本包含多个大写字母开头的单词，可能是姓名
        String[] words = text.split("\\s+");
        if (words.length >= 2) {
            int capitalizedCount = 0;
            for (String word : words) {
                if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                    capitalizedCount++;
                }
            }
            return capitalizedCount >= 2;
        }
        return false;
    }

    /**
     * 清理作者名称
     */
    private String cleanAuthorName(String name) {
        if (name == null) return "";

        // 移除多余的空格
        name = name.replaceAll("\\s+", " ");

        // 移除标点符号（除了连字符和撇号）
        name = name.replaceAll("[^a-zA-Z\\s\\-']", " ");

        // 再次移除多余的空格
        name = name.replaceAll("\\s+", " ").trim();

        return name;
    }

    /**
     * 改进的机构提取方法
     */
    private Set<String> extractInstitutionsImproved(String address) {
        Set<String> institutions = new HashSet<>();

        if (address == null || address.trim().isEmpty()) {
            return institutions;
        }

        // 处理格式：[Author Names] Institution, City, Country
        // 分割不同的地址条目
        String[] entries = address.split(";");

        for (String entry : entries) {
            // 移除方括号中的作者名称
            String cleaned = entry.replaceAll("\\[.*?\\]", "").trim();

            // 提取机构名称（通常是第一个逗号之前的部分）
            if (cleaned.contains(",")) {
                String[] parts = cleaned.split(",");
                if (parts.length > 0) {
                    String institution = cleanInstitutionName(parts[0].trim());
                    if (institution.length() > 5) { // 过滤太短的结果
                        institutions.add(institution);
                    }
                }
            } else if (cleaned.length() > 5) {
                // 没有逗号的情况，可能整个就是机构名称
                String institution = cleanInstitutionName(cleaned);
                if (institution.length() > 5) {
                    institutions.add(institution);
                }
            }
        }

        // 备用方法：使用关键词匹配
        if (institutions.isEmpty()) {
            institutions = extractInstitutionsByKeywords(address);
        }

        return institutions;
    }

    /**
     * 清理机构名称
     */
    private String cleanInstitutionName(String institution) {
        if (institution == null) return "";

        // 移除多余的符号和空格
        institution = institution.replaceAll("\\s+", " ");
        institution = institution.replaceAll("^[\\s,;]+", "");
        institution = institution.replaceAll("[\\s,;]+$", "");

        // 移除数字编号（如部门编号）
        institution = institution.replaceAll("\\b\\d{1,3}\\b", "").trim();

        // 标准化缩写
        institution = institution.replace("Univ.", "University");
        institution = institution.replace("Inst.", "Institute");
        institution = institution.replace("Lab.", "Laboratory");
        institution = institution.replace("Dept.", "Department");
        institution = institution.replace("Coll.", "College");
        institution = institution.replace("Hosp.", "Hospital");
        institution = institution.replace("Sch.", "School");
        institution = institution.replace("Ctr.", "Center");

        return institution.trim();
    }

    /**
     * 检查文本是否包含机构关键词
     */
    private boolean containsInstitutionKeyword(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        String[] keywords = {
                "university", "univ", "institute", "inst", "college", "coll",
                "hospital", "hosp", "school", "academy", "center", "centre",
                "laboratory", "lab", "department", "dept", "faculty",
                "research", "foundation", "corporation", "corp", "company",
                "ministry", "bureau", "agency", "commission", "council",
                "polytechnic", "politecnico", "universitat", "universiteit",
                "universite", "universita", "universidad", "universidade"
        };

        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 基于关键词提取机构（备用方法）
     */
    private Set<String> extractInstitutionsByKeywords(String address) {
        Set<String> institutions = new HashSet<>();

        // 机构关键词
        String[] keywords = {
                "University", "Univ", "Institute", "Inst", "Laboratory", "Lab",
                "College", "Coll", "Hospital", "Hosp", "School", "Academy",
                "Center", "Centre", "Department", "Faculty", "Research"
        };

        // 使用正则表达式查找包含关键词的机构名称
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile(
                    "([^,;\\[\\]]{0,50}" + Pattern.quote(keyword) + "[^,;\\[\\]]{0,30})",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(address);

            while (matcher.find()) {
                String institution = cleanInstitutionName(matcher.group(1).trim());
                if (institution.length() > 5 && !institution.matches(".*\\d{5,}.*")) {
                    institutions.add(institution);
                }
            }
        }

        return institutions;
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
                // 尝试多个关键词字段
                String keywordStr = paper.getKeyword();

                // 如果keyword字段为空，尝试keyword_plus字段
                if (keywordStr == null || keywordStr.trim().isEmpty()) {
                    keywordStr = paper.getKeyword_plus();
                }

                // 如果还是空，尝试subject_extended字段
                if (keywordStr == null || keywordStr.trim().isEmpty()) {
                    keywordStr = paper.getSubject_extended();
                }

                // 处理关键词
                if (keywordStr != null && !keywordStr.trim().isEmpty()) {
                    String[] keywords = keywordStr.split("[;,]");
                    for (String keyword : keywords) {
                        String cleanKeyword = keyword.trim().toLowerCase();

                        // 过滤无效关键词
                        if (!cleanKeyword.isEmpty() &&
                                !cleanKeyword.equals("null") &&
                                !cleanKeyword.equals("na") &&
                                !cleanKeyword.equals("n/a") &&
                                !cleanKeyword.equals("none") &&
                                !cleanKeyword.equals("-") &&
                                cleanKeyword.length() > 2) {

                            // 额外清理：移除可能的引号或其他标点
                            cleanKeyword = cleanKeyword.replaceAll("[\"']", "").trim();

                            if (!cleanKeyword.isEmpty()) {
                                keywordCount.merge(cleanKeyword, 1, Integer::sum);
                            }
                        }
                    }
                }
            }

            // 只保留前10个关键词
            Map<String, Integer> topKeywords = keywordCount.entrySet().stream()
                    .filter(e -> !e.getKey().equals("null") && !e.getKey().isEmpty()) // 再次过滤
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            if (!topKeywords.isEmpty()) {
                yearlyKeywords.put(year, topKeywords);
            }
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
                // 使用改进的作者解析
                if (paper.getAuthor_fullname() != null) {
                    uniqueAuthors.addAll(parseAuthors(paper.getAuthor_fullname()));
                }

                if (paper.getJournal_title_source() != null) {
                    uniqueJournals.add(paper.getJournal_title_source().trim());
                }

                // 使用改进的国家提取
                if (paper.getAddress() != null) {
                    uniqueCountries.addAll(extractCountriesFromAddress(paper.getAddress()));
                }
                if (paper.getReprint_address() != null) {
                    uniqueCountries.addAll(extractCountriesFromAddress(paper.getReprint_address()));
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