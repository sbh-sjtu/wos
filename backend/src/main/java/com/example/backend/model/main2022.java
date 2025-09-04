package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 论文数据的实体类
 * 基于实际的SQL Server表结构进行调整
 */
@Entity
@Table(name = "Wos_2000")  // 根据实际的表名格式调整，如 Wos_2023, Wos_2024 等
@Data
@Document(indexName = "main")
public class main2022 {
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_temp")
    private Integer seq_temp;

    @Column(name = "wos_uid", length = 15)
    private String wos_uid;

    @Column(name = "database", length = 50)
    private String database;

    @Column(name = "sortdate", length = 15)
    private String sortdate;

    @Column(name = "pubyear")
    private String pubyear;

    @Column(name = "has_abstract", length = 5)
    private String has_abstract;

    @Column(name = "coverdate", length = 15)
    private String coverdate;

    @Column(name = "pubmonth", length = 8)
    private String pubmonth;

    @Column(name = "vol", length = 15)
    private String vol;

    @Column(name = "issue", length = 15)
    private String issue;

    @Column(name = "special_issue", length = 15)
    private String special_issue;

    @Column(name = "supplement", length = 20)
    private String supplement;

    @Column(name = "early_access_date", length = 20)
    private String early_access_date;

    @Column(name = "early_access_month", length = 4)
    private String early_access_month;

    @Column(name = "early_access_year", length = 4)
    private String early_access_year;

    @Column(name = "article_pubtype", length = 20)  // 注意：Python脚本中是article_pubtype，不是article_type
    private String article_type;

    @Column(name = "page_count", length = 15)
    private String page_count;

    @Column(name = "page_begin", length = 15)
    private String page_begin;

    @Column(name = "page_end", length = 15)
    private String page_end;

    @Column(name = "journal_title_source", length = 500)
    private String journal_title_source;

    @Column(name = "journal_title_abbrev", length = 50)
    private String journal_title_abbrev;

    @Column(name = "journal_title_iso", length = 100)
    private String journal_title_iso;

    @Column(name = "journal_title_11", length = 50)
    private String journal_title_11;

    @Column(name = "journal_title_29", length = 50)
    private String journal_title_29;

    @Column(name = "article_title", columnDefinition = "VARCHAR(MAX)")
    private String article_title;

    @Column(name = "article_doctype", length = 100)
    private String article_doctype;

    @Column(name = "heading", length = 100)
    private String heading;

    @Column(name = "subheadings", length = 100)
    private String subheadings;

    @Column(name = "subject_traditional", length = 500)
    private String subject_traditional;

    @Column(name = "subject_extended", length = 500)
    private String subject_extended;

    @Column(name = "fund_text", columnDefinition = "VARCHAR(MAX)")
    private String fund_text;

    @Column(name = "keyword", columnDefinition = "VARCHAR(MAX)")
    private String keyword;

    @Column(name = "keyword_plus", columnDefinition = "VARCHAR(MAX)")
    private String keyword_plus;

    @Column(name = "abstract", columnDefinition = "VARCHAR(MAX)")  // 注意：Python脚本中是abstract，不是abstract_text
    private String abstract_text;

    @Column(name = "ids", length = 10)
    private String ids;

    @Column(name = "bib_id", length = 100)
    private String bib_id;

    @Column(name = "bib_pagecount", length = 10)
    private String bib_pagecount;

    @Column(name = "reviewed_work", length = 800)
    private String reviewed_work;

    @Column(name = "languages", length = 50)
    private String languages;

    @Column(name = "rw_authors", length = 500)
    private String rw_authors;

    @Column(name = "rw_year", length = 10)
    private String rw_year;

    @Column(name = "rw_language", length = 50)
    private String rw_language;

    @Column(name = "book_note", length = 50)
    private String book_note;

    @Column(name = "bk_binding", length = 50)
    private String bk_binding;

    @Column(name = "bk_publisher", length = 500)
    private String bk_publisher;

    @Column(name = "bk_prepay", length = 6)
    private String bk_prepay;

    @Column(name = "bk_ordering", length = 500)
    private String bk_ordering;

    @Column(name = "identifier_accession_no", length = 20)
    private String identifier_accession_no;

    @Column(name = "identifier_issn", length = 20)
    private String identifier_issn;

    @Column(name = "identifier_eissn", length = 20)
    private String identifier_eissn;

    @Column(name = "identifier_isbn", length = 50)
    private String identifier_isbn;

    @Column(name = "identifier_eisbn", length = 50)
    private String identifier_eisbn;

    @Column(name = "identifier_doi", length = 100)
    private String identifier_doi;

    @Column(name = "identifier_pmid", length = 20)
    private String identifier_pmid;

    @Column(name = "normalized_doctype", length = 50)
    private String normalized_doctype;

    @Column(name = "is_OA", length = 8)
    private String is_OA;

    @Column(name = "oases", length = 1000)
    private String oases;

    @Column(name = "subj_group_macro_id", length = 10)
    private String subj_group_macro_id;

    @Column(name = "subj_group_macro_value", length = 100)
    private String subj_group_macro_value;

    @Column(name = "subj_group_meso_id", length = 200)
    private String subj_group_meso_id;

    @Column(name = "subj_group_meso_value", length = 200)
    private String subj_group_meso_value;

    @Column(name = "subj_group_micro_id", length = 50)
    private String subj_group_micro_id;

    @Column(name = "subj_group_micro_value", length = 500)
    private String subj_group_micro_value;

    @Column(name = "author_fullname", columnDefinition = "VARCHAR(MAX)")
    private String author_fullname;

    @Column(name = "author_displayname", columnDefinition = "VARCHAR(MAX)")
    private String author_displayname;

    @Column(name = "author_wosname", columnDefinition = "VARCHAR(MAX)")
    private String author_wosname;

    @Column(name = "grant_info", columnDefinition = "VARCHAR(MAX)")
    private String grant_info;

    @Column(name = "address", columnDefinition = "VARCHAR(MAX)")
    private String address;

    @Column(name = "reprint_address", columnDefinition = "VARCHAR(MAX)")
    private String reprint_address;

    @Column(name = "email", columnDefinition = "VARCHAR(MAX)")
    private String email;

    @Column(name = "contributor", columnDefinition = "VARCHAR(MAX)")
    private String contributor;

    @Column(name = "publisher", columnDefinition = "VARCHAR(MAX)")
    private String publisher;

    @Column(name = "publisher_unified", columnDefinition = "VARCHAR(MAX)")
    private String publisher_unified;

    @Column(name = "publisher_display", columnDefinition = "VARCHAR(MAX)")
    private String publisher_display;

    // Getter和Setter方法保持不变...
    public void setSeq_temp(Integer seqTemp) {
        this.seq_temp = seqTemp;
    }

    public Integer getSeq_temp() {
        return seq_temp;
    }

    public String getWos_uid() {
        return wos_uid;
    }

    public void setWos_uid(String wos_uid) {
        this.wos_uid = wos_uid;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSortdate() {
        return sortdate;
    }

    public void setSortdate(String sortdate) {
        this.sortdate = sortdate;
    }

    public String getPubyear() {  // 注意这里改为Integer
        return pubyear;
    }

    public void setPubyear(String pubyear) {  // 注意这里改为Integer
        this.pubyear = pubyear;
    }

    // ... 其他getter和setter方法保持原样，只需要注意pubyear的类型变更
    // 为了节省空间，这里不重复所有的getter和setter

    public String getHas_abstract() {
        return has_abstract;
    }

    public void setHas_abstract(String has_abstract) {
        this.has_abstract = has_abstract;
    }

    public String getCoverdate() {
        return coverdate;
    }

    public void setCoverdate(String coverdate) {
        this.coverdate = coverdate;
    }

    public String getPubmonth() {
        return pubmonth;
    }

    public void setPubmonth(String pubmonth) {
        this.pubmonth = pubmonth;
    }

    public String getVol() {
        return vol;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getSpecial_issue() {
        return special_issue;
    }

    public void setSpecial_issue(String special_issue) {
        this.special_issue = special_issue;
    }

    public String getSupplement() {
        return supplement;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }

    public String getEarly_access_date() {
        return early_access_date;
    }

    public void setEarly_access_date(String early_access_date) {
        this.early_access_date = early_access_date;
    }

    public String getEarly_access_month() {
        return early_access_month;
    }

    public void setEarly_access_month(String early_access_month) {
        this.early_access_month = early_access_month;
    }

    public String getEarly_access_year() {
        return early_access_year;
    }

    public void setEarly_access_year(String early_access_year) {
        this.early_access_year = early_access_year;
    }

    public String getArticle_type() {
        return article_type;
    }

    public void setArticle_type(String article_type) {
        this.article_type = article_type;
    }

    public String getPage_count() {
        return page_count;
    }

    public void setPage_count(String page_count) {
        this.page_count = page_count;
    }

    public String getPage_begin() {
        return page_begin;
    }

    public void setPage_begin(String page_begin) {
        this.page_begin = page_begin;
    }

    public String getPage_end() {
        return page_end;
    }

    public void setPage_end(String page_end) {
        this.page_end = page_end;
    }

    public String getJournal_title_source() {
        return journal_title_source;
    }

    public void setJournal_title_source(String journal_title_source) {
        this.journal_title_source = journal_title_source;
    }

    public String getJournal_title_abbrev() {
        return journal_title_abbrev;
    }

    public void setJournal_title_abbrev(String journal_title_abbrev) {
        this.journal_title_abbrev = journal_title_abbrev;
    }

    public String getJournal_title_iso() {
        return journal_title_iso;
    }

    public void setJournal_title_iso(String journal_title_iso) {
        this.journal_title_iso = journal_title_iso;
    }

    public String getJournal_title_11() {
        return journal_title_11;
    }

    public void setJournal_title_11(String journal_title_11) {
        this.journal_title_11 = journal_title_11;
    }

    public String getJournal_title_29() {
        return journal_title_29;
    }

    public void setJournal_title_29(String journal_title_29) {
        this.journal_title_29 = journal_title_29;
    }

    public String getArticle_title() {
        return article_title;
    }

    public void setArticle_title(String article_title) {
        this.article_title = article_title;
    }

    public String getArticle_doctype() {
        return article_doctype;
    }

    public void setArticle_doctype(String article_doctype) {
        this.article_doctype = article_doctype;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSubheadings() {
        return subheadings;
    }

    public void setSubheadings(String subheadings) {
        this.subheadings = subheadings;
    }

    public String getSubject_traditional() {
        return subject_traditional;
    }

    public void setSubject_traditional(String subject_traditional) {
        this.subject_traditional = subject_traditional;
    }

    public String getSubject_extended() {
        return subject_extended;
    }

    public void setSubject_extended(String subject_extended) {
        this.subject_extended = subject_extended;
    }

    public String getFund_text() {
        return fund_text;
    }

    public void setFund_text(String fund_text) {
        this.fund_text = fund_text;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword_plus() {
        return keyword_plus;
    }

    public void setKeyword_plus(String keyword_plus) {
        this.keyword_plus = keyword_plus;
    }

    public String getAbstract_text() {
        return abstract_text;
    }

    public void setAbstract_text(String abstract_text) {
        this.abstract_text = abstract_text;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getBib_id() {
        return bib_id;
    }

    public void setBib_id(String bib_id) {
        this.bib_id = bib_id;
    }

    public String getBib_pagecount() {
        return bib_pagecount;
    }

    public void setBib_pagecount(String bib_pagecount) {
        this.bib_pagecount = bib_pagecount;
    }

    public String getReviewed_work() {
        return reviewed_work;
    }

    public void setReviewed_work(String reviewed_work) {
        this.reviewed_work = reviewed_work;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getRw_authors() {
        return rw_authors;
    }

    public void setRw_authors(String rw_authors) {
        this.rw_authors = rw_authors;
    }

    public String getRw_year() {
        return rw_year;
    }

    public void setRw_year(String rw_year) {
        this.rw_year = rw_year;
    }

    public String getRw_language() {
        return rw_language;
    }

    public void setRw_language(String rw_language) {
        this.rw_language = rw_language;
    }

    public String getBook_note() {
        return book_note;
    }

    public void setBook_note(String book_note) {
        this.book_note = book_note;
    }

    public String getBk_binding() {
        return bk_binding;
    }

    public void setBk_binding(String bk_binding) {
        this.bk_binding = bk_binding;
    }

    public String getBk_publisher() {
        return bk_publisher;
    }

    public void setBk_publisher(String bk_publisher) {
        this.bk_publisher = bk_publisher;
    }

    public String getBk_prepay() {
        return bk_prepay;
    }

    public void setBk_prepay(String bk_prepay) {
        this.bk_prepay = bk_prepay;
    }

    public String getBk_ordering() {
        return bk_ordering;
    }

    public void setBk_ordering(String bk_ordering) {
        this.bk_ordering = bk_ordering;
    }

    public String getIdentifier_accession_no() {
        return identifier_accession_no;
    }

    public void setIdentifier_accession_no(String identifier_accession_no) {
        this.identifier_accession_no = identifier_accession_no;
    }

    public String getIdentifier_issn() {
        return identifier_issn;
    }

    public void setIdentifier_issn(String identifier_issn) {
        this.identifier_issn = identifier_issn;
    }

    public String getIdentifier_eissn() {
        return identifier_eissn;
    }

    public void setIdentifier_eissn(String identifier_eissn) {
        this.identifier_eissn = identifier_eissn;
    }

    public String getIdentifier_isbn() {
        return identifier_isbn;
    }

    public void setIdentifier_isbn(String identifier_isbn) {
        this.identifier_isbn = identifier_isbn;
    }

    public String getIdentifier_eisbn() {
        return identifier_eisbn;
    }

    public void setIdentifier_eisbn(String identifier_eisbn) {
        this.identifier_eisbn = identifier_eisbn;
    }

    public String getIdentifier_doi() {
        return identifier_doi;
    }

    public void setIdentifier_doi(String identifier_doi) {
        this.identifier_doi = identifier_doi;
    }

    public String getIdentifier_pmid() {
        return identifier_pmid;
    }

    public void setIdentifier_pmid(String identifier_pmid) {
        this.identifier_pmid = identifier_pmid;
    }

    public String getNormalized_doctype() {
        return normalized_doctype;
    }

    public void setNormalized_doctype(String normalized_doctype) {
        this.normalized_doctype = normalized_doctype;
    }

    public String getIs_OA() {
        return is_OA;
    }

    public void setIs_OA(String is_OA) {
        this.is_OA = is_OA;
    }

    public String getOases() {
        return oases;
    }

    public void setOases(String oases) {
        this.oases = oases;
    }

    public String getSubj_group_macro_id() {
        return subj_group_macro_id;
    }

    public void setSubj_group_macro_id(String subj_group_macro_id) {
        this.subj_group_macro_id = subj_group_macro_id;
    }

    public String getSubj_group_macro_value() {
        return subj_group_macro_value;
    }

    public void setSubj_group_macro_value(String subj_group_macro_value) {
        this.subj_group_macro_value = subj_group_macro_value;
    }

    public String getSubj_group_meso_id() {
        return subj_group_meso_id;
    }

    public void setSubj_group_meso_id(String subj_group_meso_id) {
        this.subj_group_meso_id = subj_group_meso_id;
    }

    public String getSubj_group_meso_value() {
        return subj_group_meso_value;
    }

    public void setSubj_group_meso_value(String subj_group_meso_value) {
        this.subj_group_meso_value = subj_group_meso_value;
    }

    public String getSubj_group_micro_id() {
        return subj_group_micro_id;
    }

    public void setSubj_group_micro_id(String subj_group_micro_id) {
        this.subj_group_micro_id = subj_group_micro_id;
    }

    public String getSubj_group_micro_value() {
        return subj_group_micro_value;
    }

    public void setSubj_group_micro_value(String subj_group_micro_value) {
        this.subj_group_micro_value = subj_group_micro_value;
    }

    public String getAuthor_fullname() {
        return author_fullname;
    }

    public void setAuthor_fullname(String author_fullname) {
        this.author_fullname = author_fullname;
    }

    public String getAuthor_displayname() {
        return author_displayname;
    }

    public void setAuthor_displayname(String author_displayname) {
        this.author_displayname = author_displayname;
    }

    public String getAuthor_wosname() {
        return author_wosname;
    }

    public void setAuthor_wosname(String author_wosname) {
        this.author_wosname = author_wosname;
    }

    public String getGrant_info() {
        return grant_info;
    }

    public void setGrant_info(String grant_info) {
        this.grant_info = grant_info;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReprint_address() {
        return reprint_address;
    }

    public void setReprint_address(String reprint_address) {
        this.reprint_address = reprint_address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisher_unified() {
        return publisher_unified;
    }

    public void setPublisher_unified(String publisher_unified) {
        this.publisher_unified = publisher_unified;
    }

    public String getPublisher_display() {
        return publisher_display;
    }

    public void setPublisher_display(String publisher_display) {
        this.publisher_display = publisher_display;
    }
}