package com.example.backend.service.impl;

import com.example.backend.model.main2022;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class DataSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private TableSelectorService tableSelectorService;

    /**
     * 将多个年份表的数据同步到Elasticsearch
     */
    public String syncDataToElasticsearch() {
        try {
            // 检查并创建索引
            IndexOperations indexOps = elasticsearchOperations.indexOps(main2022.class);

            if (!indexOps.exists()) {
                System.out.println("创建索引...");
                indexOps.create();
                indexOps.putMapping();
                System.out.println("索引创建成功");
            } else {
                System.out.println("索引已存在");
            }

            // 获取所有支持的表
            List<String> tableNames = tableSelectorService.getAllSupportedTables();
            System.out.println("开始同步表: " + tableNames);

            List<main2022> allPapers = new ArrayList<>();
            int totalFromDB = 0;

            // 从每个表查询数据
            for (String tableName : tableNames) {
                try {
                    System.out.println("正在查询表: " + tableName);

                    // 注意：移除seq_temp字段，确保字段对应
                    String sql = "SELECT TOP 200 wos_uid, database, sortdate, pubyear, has_abstract, " +
                            "coverdate, pubmonth, vol, issue, special_issue, supplement, " +
                            "early_access_date, early_access_month, early_access_year, article_type, " +
                            "page_count, page_begin, page_end, journal_title_source, journal_title_abbrev, " +
                            "journal_title_iso, journal_title_11, journal_title_29, article_title, " +
                            "article_doctype, heading, subheadings, subject_traditional, subject_extended, " +
                            "fund_text, keyword, keyword_plus, abstract_text, ids, bib_id, bib_pagecount, " +
                            "reviewed_work, languages, rw_authors, rw_year, rw_language, book_note, " +
                            "bk_binding, bk_publisher, bk_prepay, bk_ordering, identifier_accession_no, " +
                            "identifier_issn, identifier_eissn, identifier_isbn, identifier_eisbn, " +
                            "identifier_doi, identifier_pmid, normalized_doctype, is_OA, oases, " +
                            "subj_group_macro_id, subj_group_macro_value, subj_group_meso_id, " +
                            "subj_group_meso_value, subj_group_micro_id, subj_group_micro_value, " +
                            "author_fullname, author_displayname, author_wosname, grant_info, " +
                            "address, reprint_address, email, contributor, publisher, " +
                            "publisher_unified, publisher_display " +
                            "FROM [" + tableName + "]";

                    List<main2022> papers = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(main2022.class));

                    System.out.println("从表 " + tableName + " 查询到 " + papers.size() + " 条数据");
                    totalFromDB += papers.size();
                    allPapers.addAll(papers);

                } catch (Exception e) {
                    System.err.println("查询表 " + tableName + " 失败: " + e.getMessage());
                    // 继续处理其他表
                }
            }

            System.out.println("总共从数据库查询到 " + totalFromDB + " 条数据");

            if (allPapers.isEmpty()) {
                return "数据库中没有数据";
            }

            // 清理数据
            for (main2022 paper : allPapers) {
                // 确保必要字段不为null
                if (paper.getPubyear() == null) {
                    paper.setPubyear("Unknown");
                }
                if (paper.getKeyword() == null) {
                    paper.setKeyword("");
                }
                if (paper.getArticle_title() == null) {
                    paper.setArticle_title("");
                }
                // 确保主键wos_uid不为null
                if (paper.getWos_uid() == null || paper.getWos_uid().trim().isEmpty()) {
                    paper.setWos_uid("UNKNOWN_" + System.currentTimeMillis() + "_" + Math.random());
                }
            }

            // 批量插入到Elasticsearch
            System.out.println("开始批量插入到Elasticsearch...");

            // 分批处理，每次50条
            int batchSize = 50;
            int totalBatches = (int) Math.ceil((double) allPapers.size() / batchSize);
            int successCount = 0;

            for (int i = 0; i < totalBatches; i++) {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, allPapers.size());
                List<main2022> batch = allPapers.subList(start, end);

                try {
                    // 逐个保存以避免批量操作问题
                    for (main2022 paper : batch) {
                        try {
                            elasticsearchOperations.save(paper);
                            successCount++;
                        } catch (Exception singleError) {
                            System.err.println("单条记录插入失败，wos_uid: " + paper.getWos_uid() +
                                    ", 错误: " + singleError.getMessage());
                        }
                    }

                    System.out.println("已处理批次 " + (i + 1) + "/" + totalBatches +
                            " (" + batch.size() + " 条记录)，成功: " + successCount);
                } catch (Exception e) {
                    System.err.println("批次 " + (i + 1) + " 处理失败: " + e.getMessage());
                }
            }

            // 强制刷新索引
            indexOps.refresh();

            // 验证导入结果
            long count = elasticsearchOperations.count(Query.findAll(), main2022.class);

            return "多表数据同步完成。查询表: " + tableNames +
                    ", 数据库总数: " + totalFromDB + " 条，成功插入: " + successCount +
                    " 条，Elasticsearch总数: " + count;

        } catch (Exception e) {
            e.printStackTrace();
            return "多表数据同步失败: " + e.getMessage();
        }
    }

    /**
     * 检查多个年份表的数据
     */
    public String checkMySQLData() {
        try {
            List<String> tableNames = tableSelectorService.getAllSupportedTables();
            StringBuilder result = new StringBuilder();
            result.append("多表数据检查结果:\n");

            int totalCount = 0;

            for (String tableName : tableNames) {
                try {
                    String countSql = "SELECT COUNT(*) FROM [" + tableName + "]";
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                    totalCount += (count != null ? count : 0);

                    result.append("表 ").append(tableName).append(": ").append(count).append(" 条记录\n");

                    // 获取样本数据（只查询前几个表的样本）
                    if (tableNames.indexOf(tableName) < 2) {
                        String sampleSql = "SELECT TOP 2 wos_uid, pubyear, article_title, keyword FROM [" + tableName + "]";
                        List<main2022> samples = jdbcTemplate.query(sampleSql, new BeanPropertyRowMapper<>(main2022.class));

                        for (main2022 sample : samples) {
                            result.append("  样本 - wos_uid: ").append(sample.getWos_uid())
                                    .append(", pubyear: '").append(sample.getPubyear())
                                    .append("', title: ").append(sample.getArticle_title() != null ?
                                            sample.getArticle_title().substring(0, Math.min(50, sample.getArticle_title().length())) : "null")
                                    .append("\n");
                        }
                    }

                } catch (Exception e) {
                    result.append("表 ").append(tableName).append(": 查询失败 - ").append(e.getMessage()).append("\n");
                }
            }

            result.append("总记录数: ").append(totalCount).append("\n");
            result.append("支持的年份范围: ").append(tableSelectorService.getSupportedYearRange()).append("\n");

            return result.toString();

        } catch (Exception e) {
            return "多表数据检查失败: " + e.getMessage();
        }
    }

    /**
     * 清空Elasticsearch索引
     */
    public String clearElasticsearchIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(main2022.class);

            if (indexOps.exists()) {
                indexOps.delete();
                System.out.println("索引已删除");
            }

            indexOps.create();
            indexOps.putMapping();
            System.out.println("索引重新创建完成");

            return "Elasticsearch索引已清空并重新创建";

        } catch (Exception e) {
            return "清空索引失败: " + e.getMessage();
        }
    }

    /**
     * 新增：按年份范围同步数据
     */
    public String syncDataByYearRange(Integer startYear, Integer endYear) {
        try {
            List<String> tableNames = tableSelectorService.determineTablesByYearRange(startYear, endYear);

            if (tableNames.isEmpty()) {
                return "指定年份范围内没有可同步的表";
            }

            System.out.println("按年份范围同步，年份: " + startYear + "-" + endYear + ", 表: " + tableNames);

            // 检查并创建索引
            IndexOperations indexOps = elasticsearchOperations.indexOps(main2022.class);
            if (!indexOps.exists()) {
                indexOps.create();
                indexOps.putMapping();
            }

            List<main2022> allPapers = new ArrayList<>();
            int totalFromDB = 0;

            // 从指定年份的表查询数据
            for (String tableName : tableNames) {
                try {
                    String sql = "SELECT TOP 500 wos_uid, database, sortdate, pubyear, has_abstract, " +
                            "coverdate, pubmonth, vol, issue, special_issue, supplement, " +
                            "early_access_date, early_access_month, early_access_year, article_type, " +
                            "page_count, page_begin, page_end, journal_title_source, journal_title_abbrev, " +
                            "journal_title_iso, journal_title_11, journal_title_29, article_title, " +
                            "article_doctype, heading, subheadings, subject_traditional, subject_extended, " +
                            "fund_text, keyword, keyword_plus, abstract_text, ids, bib_id, bib_pagecount, " +
                            "reviewed_work, languages, rw_authors, rw_year, rw_language, book_note, " +
                            "bk_binding, bk_publisher, bk_prepay, bk_ordering, identifier_accession_no, " +
                            "identifier_issn, identifier_eissn, identifier_isbn, identifier_eisbn, " +
                            "identifier_doi, identifier_pmid, normalized_doctype, is_OA, oases, " +
                            "subj_group_macro_id, subj_group_macro_value, subj_group_meso_id, " +
                            "subj_group_meso_value, subj_group_micro_id, subj_group_micro_value, " +
                            "author_fullname, author_displayname, author_wosname, grant_info, " +
                            "address, reprint_address, email, contributor, publisher, " +
                            "publisher_unified, publisher_display " +
                            "FROM [" + tableName + "]";

                    List<main2022> papers = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(main2022.class));
                    totalFromDB += papers.size();
                    allPapers.addAll(papers);

                } catch (Exception e) {
                    System.err.println("查询表 " + tableName + " 失败: " + e.getMessage());
                }
            }

            if (allPapers.isEmpty()) {
                return "指定年份范围内没有数据";
            }

            // 批量插入到Elasticsearch
            int successCount = 0;
            for (main2022 paper : allPapers) {
                try {
                    // 确保主键不为空
                    if (paper.getWos_uid() == null || paper.getWos_uid().trim().isEmpty()) {
                        paper.setWos_uid("UNKNOWN_" + System.currentTimeMillis() + "_" + Math.random());
                    }
                    elasticsearchOperations.save(paper);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("保存记录失败: " + e.getMessage());
                }
            }

            indexOps.refresh();
            long totalInES = elasticsearchOperations.count(Query.findAll(), main2022.class);

            return "按年份范围同步完成。年份: " + startYear + "-" + endYear +
                    ", 查询表: " + tableNames +
                    ", 数据库查询: " + totalFromDB + " 条，成功插入: " + successCount +
                    " 条，ES总数: " + totalInES;

        } catch (Exception e) {
            e.printStackTrace();
            return "按年份范围同步失败: " + e.getMessage();
        }
    }
}