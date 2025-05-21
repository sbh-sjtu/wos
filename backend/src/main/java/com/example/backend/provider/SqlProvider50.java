package com.example.backend.provider;
import com.example.backend.config.SearchFilter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import java.util.List;
public class SqlProvider50 {
    public String advancedSearch(@Param("filters") List<SearchFilter> filters) {
        return new SQL() {{
            SELECT("*");
            FROM("main");

            if (filters != null && !filters.isEmpty()) {
                WHERE(buildDynamicSql(filters));
            }
            //最多50条记录
            LIMIT("50");
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
}
