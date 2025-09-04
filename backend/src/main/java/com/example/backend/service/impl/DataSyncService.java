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

@Service
public class DataSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 将MySQL数据同步到Elasticsearch
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

            // 从MySQL查询数据
            System.out.println("开始从MySQL查询数据...");
            String sql = "SELECT * FROM main LIMIT 1000"; // 先同步1000条测试
            List<main2022> papers = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(main2022.class));

            System.out.println("从MySQL查询到 " + papers.size() + " 条数据");

            if (papers.isEmpty()) {
                return "MySQL数据库中没有数据";
            }

            // 清理数据
            for (main2022 paper : papers) {
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
            }

            // 批量插入到Elasticsearch
            System.out.println("开始批量插入到Elasticsearch...");

            // 分批处理，每次100条
            int batchSize = 50; // 减少批次大小
            int totalBatches = (int) Math.ceil((double) papers.size() / batchSize);
            int successCount = 0;

            for (int i = 0; i < totalBatches; i++) {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, papers.size());
                List<main2022> batch = papers.subList(start, end);

                try {
                    // 逐个保存以避免批量操作问题
                    for (main2022 paper : batch) {
                        try {
                            elasticsearchOperations.save(paper);
                            successCount++;
                        } catch (Exception singleError) {
                            System.err.println("单条记录插入失败，seq_temp: " + paper.getSeq_temp() +
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

            return "数据同步完成。MySQL: " + papers.size() + " 条，成功插入: " + successCount + " 条，Elasticsearch总数: " + count;

        } catch (Exception e) {
            e.printStackTrace();
            return "数据同步失败: " + e.getMessage();
        }
    }

    /**
     * 检查MySQL数据
     */
    public String checkMySQLData() {
        try {
            String countSql = "SELECT COUNT(*) FROM main";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);

            String sampleSql = "SELECT seq_temp, wos_uid, pubyear, article_title, keyword FROM main LIMIT 5";
            List<main2022> samples = jdbcTemplate.query(sampleSql, new BeanPropertyRowMapper<>(main2022.class));

            StringBuilder result = new StringBuilder();
            result.append("MySQL数据检查结果:\n");
            result.append("总记录数: ").append(count).append("\n");
            result.append("样本数据:\n");

            for (main2022 sample : samples) {
                result.append("  seq_temp: ").append(sample.getSeq_temp())
                        .append(", pubyear: '").append(sample.getPubyear())
                        .append("', title: ").append(sample.getArticle_title() != null ?
                                sample.getArticle_title().substring(0, Math.min(50, sample.getArticle_title().length())) : "null")
                        .append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            return "MySQL数据检查失败: " + e.getMessage();
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
}