package com.example.backend.provider;

import com.example.backend.config.SearchFilter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import java.util.List;

public class SqlProvider {

    public String advancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("TOP 500 *");
            // 根据实际的表名格式调整，可能需要根据年份动态生成表名
            FROM("[Wos_2022]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
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
        // 转义 SQL Server 中的特殊字符
        return keyword.replace("'", "''")           // 单引号转义
                .replace("[", "[[]")           // 方括号转义
                .replace("%", "[%]")           // 百分号转义
                .replace("_", "[_]");          // 下划线转义
    }
}
