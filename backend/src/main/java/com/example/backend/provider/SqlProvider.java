package com.example.backend.provider;

import com.example.backend.config.SearchFilter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import java.util.List;

public class SqlProvider {

    /**
     * 高级搜索（限制500条）
     */
    public String advancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("TOP 500 *");
            FROM("[Wos_2022]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    /**
     * 高级搜索（获取所有数据，完全避免排序）
     * 注意：这将按照表的自然存储顺序返回数据
     */
    public String advancedSearchAll(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("*");
            FROM("[Wos_2022]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    /**
     * 计算符合条件的总数量
     */
    public String countAdvancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("COUNT(*)");
            FROM("[Wos_2022]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    /**
     * 简化的批量查询（避免任何排序操作）
     * 使用TABLESAMPLE来随机采样数据，避免全表扫描
     */
    public String advancedSearchSample(@Param("filters") List<SearchFilter> filters,
                                       @Param("samplePercent") double samplePercent,
                                       @Param("limit") int limit) {
        StringBuilder sql = new StringBuilder();

        if (samplePercent > 0 && samplePercent < 100) {
            sql.append("SELECT TOP ").append(limit).append(" * FROM [Wos_2022] TABLESAMPLE(")
                    .append(samplePercent).append(" PERCENT)");
        } else {
            sql.append("SELECT TOP ").append(limit).append(" * FROM [Wos_2022]");
        }

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ").append(buildDynamicSql(filters));
        }

        return sql.toString();
    }

    private String buildDynamicSql(List<SearchFilter> filters) {
        StringBuilder sql = new StringBuilder();
        boolean isFirst = true;

        for (SearchFilter filter : filters) {
            if (!isFirst) {
                sql.append(" ").append(filter.getSelects().get(0)).append(" ");
            }
            isFirst = false;

            String columnCondition = buildColumnCondition(filter);
            sql.append(columnCondition);
        }

        return sql.toString();
    }

    private String buildColumnCondition(SearchFilter filter) {
        String keyword = filter.getInput();
        keyword = escapeSqlServerKeyword(keyword);

        System.out.println("keyword:" + keyword);
        StringBuilder condition = new StringBuilder();

        if ("1".equals(filter.getSelects().get(1).toString())) {
            condition.append("(keyword LIKE '%")
                    .append(keyword).append("%' OR article_title LIKE '%").append(keyword).append("%')");
        } else if ("2".equals(filter.getSelects().get(1).toString())) {
            condition.append("article_title LIKE '%").append(keyword).append("%'");
        } else if ("3".equals(filter.getSelects().get(1).toString())) {
            condition.append("author_fullname LIKE '%").append(keyword).append("%'");
        } else if ("4".equals(filter.getSelects().get(1).toString())) {
            condition.append("publisher LIKE '%").append(keyword).append("%'");
        } else if ("5".equals(filter.getSelects().get(1).toString())) {
            condition.append("pubyear LIKE '%").append(keyword).append("%'");
        } else if ("6".equals(filter.getSelects().get(1).toString())) {
            condition.append("identifier_doi LIKE '%").append(keyword).append("%'");
        }
        System.out.println("condition:" + condition.toString());
        return condition.toString();
    }

    /**
     * 转义 SQL Server 中的特殊字符
     */
    private String escapeSqlServerKeyword(String keyword) {
        if (keyword == null) return "";
        return keyword.replace("'", "''")
                .replace("[", "[[]")
                .replace("%", "[%]")
                .replace("_", "[_]");
    }
}