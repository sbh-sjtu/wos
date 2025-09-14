package com.example.backend.provider;

import com.example.backend.config.SearchFilter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import java.util.List;

public class SqlProvider {

    // ==================== 新增：学科分析专用查询方法 ====================

    /**
     * 学科分析专用多表查询
     */
    public String disciplinaryAnalysisSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                                       @Param("tableNames") List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return "SELECT * FROM [Wos_2020] WHERE 1=0"; // 返回空结果
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");

        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append("SELECT * FROM [").append(tableNames.get(i)).append("]");

            if (filters != null && !filters.isEmpty()) {
                sql.append(" WHERE ").append(buildDisciplinaryAnalysisSql(filters));
            }
        }

        sql.append(") AS combined_results ORDER BY pubyear, wos_uid");

        return sql.toString();
    }

    /**
     * 构建学科分析专用的SQL条件
     * 支持在多个字段中搜索关键词
     */
    private String buildDisciplinaryAnalysisSql(List<SearchFilter> filters) {
        StringBuilder sql = new StringBuilder();
        boolean isFirst = true;

        for (SearchFilter filter : filters) {
            if (!isFirst) {
                sql.append(" ").append(filter.getSelects().get(0)).append(" ");
            }
            isFirst = false;

            String columnCondition = buildDisciplinaryColumnCondition(filter);
            sql.append(columnCondition);
        }

        return sql.toString();
    }


    private String buildDisciplinaryColumnCondition(SearchFilter filter) {
        String keyword = filter.getInput();
        keyword = escapeSqlServerKeyword(keyword);

        System.out.println("学科分析关键词:" + keyword);
        StringBuilder condition = new StringBuilder();

        if ("1".equals(filter.getSelects().get(1).toString())) {
            // Topic搜索：在所有相关字段中搜索，包括摘要
            condition.append("(")
                    .append("keyword LIKE '%").append(keyword).append("%' OR ")
                    .append("article_title LIKE '%").append(keyword).append("%' OR ")
                    .append("subject_extended LIKE '%").append(keyword).append("%'")
                    .append(")");
        } else if ("2".equals(filter.getSelects().get(1).toString())) {
            condition.append("article_title LIKE '%").append(keyword).append("%'");
        } else if ("3".equals(filter.getSelects().get(1).toString())) {
            condition.append("author_fullname LIKE '%").append(keyword).append("%'");
        } else if ("4".equals(filter.getSelects().get(1).toString())) {
            condition.append("journal_title_source LIKE '%").append(keyword).append("%'");
        } else if ("5".equals(filter.getSelects().get(1).toString())) {
            condition.append("pubyear LIKE '%").append(keyword).append("%'");
        } else if ("6".equals(filter.getSelects().get(1).toString())) {
            condition.append("identifier_doi LIKE '%").append(keyword).append("%'");
        }

        System.out.println("学科分析条件:" + condition.toString());
        return condition.toString();
    }

    // ==================== 保持原有的所有方法不变 ====================

    /**
     * 动态多表高级搜索（限制200条）
     */
    public String advancedSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                           @Param("tableNames") List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return "SELECT TOP 200 * FROM [Wos_2020] WHERE 1=0";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 200 * FROM (");

        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append("SELECT * FROM [").append(tableNames.get(i)).append("]");

            if (filters != null && !filters.isEmpty()) {
                sql.append(" WHERE ").append(buildDynamicSql(filters));
            }
        }

        sql.append(") AS combined_results");

        return sql.toString();
    }

    /**
     * 动态多表高级搜索（获取所有数据）
     */
    public String advancedSearchAllMultiTable(@Param("filters") List<SearchFilter> filters,
                                              @Param("tableNames") List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return "SELECT * FROM [Wos_2020] WHERE 1=0";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");

        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append("SELECT * FROM [").append(tableNames.get(i)).append("]");

            if (filters != null && !filters.isEmpty()) {
                sql.append(" WHERE ").append(buildDynamicSql(filters));
            }
        }

        sql.append(") AS combined_results");

        return sql.toString();
    }

    /**
     * 动态多表计算总数量
     */
    public String countAdvancedSearchMultiTable(@Param("filters") List<SearchFilter> filters,
                                                @Param("tableNames") List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return "SELECT 0 AS total_count";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(table_count) AS total_count FROM (");

        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            sql.append("SELECT COUNT(*) AS table_count FROM [").append(tableNames.get(i)).append("]");

            if (filters != null && !filters.isEmpty()) {
                sql.append(" WHERE ").append(buildDynamicSql(filters));
            }
        }

        sql.append(") AS count_results");

        return sql.toString();
    }

    /**
     * 动态多表采样查询
     */
    public String advancedSearchSampleMultiTable(@Param("filters") List<SearchFilter> filters,
                                                 @Param("tableNames") List<String> tableNames,
                                                 @Param("samplePercent") double samplePercent,
                                                 @Param("limit") int limit) {
        if (tableNames == null || tableNames.isEmpty()) {
            return "SELECT TOP " + limit + " * FROM [Wos_2020] WHERE 1=0";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP ").append(limit).append(" * FROM (");

        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                sql.append(" UNION ALL ");
            }

            if (samplePercent > 0 && samplePercent < 100) {
                sql.append("SELECT * FROM [").append(tableNames.get(i))
                        .append("] TABLESAMPLE(").append(samplePercent).append(" PERCENT)");
            } else {
                sql.append("SELECT * FROM [").append(tableNames.get(i)).append("]");
            }

            if (filters != null && !filters.isEmpty()) {
                sql.append(" WHERE ").append(buildDynamicSql(filters));
            }
        }

        sql.append(") AS sampled_results");

        return sql.toString();
    }

    /**
     * 保留原有的单表查询方法（向后兼容）
     */
    public String advancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("TOP 200 *");
            FROM("[Wos_2020]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    public String advancedSearchAll(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("*");
            FROM("[Wos_2020]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    public String countAdvancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("COUNT(*)");
            FROM("[Wos_2020]");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
        }}.toString();
    }

    public String advancedSearchSample(@Param("filters") List<SearchFilter> filters,
                                       @Param("samplePercent") double samplePercent,
                                       @Param("limit") int limit) {
        StringBuilder sql = new StringBuilder();

        if (samplePercent > 0 && samplePercent < 100) {
            sql.append("SELECT TOP ").append(limit).append(" * FROM [Wos_2020] TABLESAMPLE(")
                    .append(samplePercent).append(" PERCENT)");
        } else {
            sql.append("SELECT TOP ").append(limit).append(" * FROM [Wos_2020]");
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
            // Topic搜索
            condition.append("(keyword LIKE '%")
                    .append(keyword).append("%' OR article_title LIKE '%").append(keyword)
                    .append("%' OR abstract_text LIKE '%").append(keyword).append("%')");
        } else if ("2".equals(filter.getSelects().get(1).toString())) {
            condition.append("article_title LIKE '%").append(keyword).append("%'");
        } else if ("3".equals(filter.getSelects().get(1).toString())) {
            condition.append("author_fullname LIKE '%").append(keyword).append("%'");
        } else if ("4".equals(filter.getSelects().get(1).toString())) {
            condition.append("journal_title_source LIKE '%").append(keyword).append("%'");
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