package com.example.backend.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 论文数据的实体类
 * 基于实际的SQL Server表结构进行调整
 * 使用MyBatis，移除JPA注解
 */
@Data
@Document(indexName = "main")
public class main2022 {
    // 移除 @Id @GeneratedValue @Column 等JPA注解
    // 保留 Elasticsearch 的 @org.springframework.data.annotation.Id
    @org.springframework.data.annotation.Id
    private Integer seq_temp;

    private String wos_uid;
    private String database;
    private String sortdate;
    private String pubyear;
    private String has_abstract;
    private String coverdate;
    private String pubmonth;
    private String vol;
    private String issue;
    private String special_issue;
    private String supplement;
    private String early_access_date;
    private String early_access_month;
    private String early_access_year;
    private String article_type;
    private String page_count;
    private String page_begin;
    private String page_end;
    private String journal_title_source;
    private String journal_title_abbrev;
    private String journal_title_iso;
    private String journal_title_11;
    private String journal_title_29;
    private String article_title;
    private String article_doctype;
    private String heading;
    private String subheadings;
    private String subject_traditional;
    private String subject_extended;
    private String fund_text;
    private String keyword;
    private String keyword_plus;
    private String abstract_text;
    private String ids;
    private String bib_id;
    private String bib_pagecount;
    private String reviewed_work;
    private String languages;
    private String rw_authors;
    private String rw_year;
    private String rw_language;
    private String book_note;
    private String bk_binding;
    private String bk_publisher;
    private String bk_prepay;
    private String bk_ordering;
    private String identifier_accession_no;
    private String identifier_issn;
    private String identifier_eissn;
    private String identifier_isbn;
    private String identifier_eisbn;
    private String identifier_doi;
    private String identifier_pmid;
    private String normalized_doctype;
    private String is_OA;
    private String oases;
    private String subj_group_macro_id;
    private String subj_group_macro_value;
    private String subj_group_meso_id;
    private String subj_group_meso_value;
    private String subj_group_micro_id;
    private String subj_group_micro_value;
    private String author_fullname;
    private String author_displayname;
    private String author_wosname;
    private String grant_info;
    private String address;
    private String reprint_address;
    private String email;
    private String contributor;
    private String publisher;
    private String publisher_unified;
    private String publisher_display;

    // 由于使用了@Data注解，Lombok会自动生成getter和setter方法
    // 原有的手动getter/setter方法可以删除，简化代码
}