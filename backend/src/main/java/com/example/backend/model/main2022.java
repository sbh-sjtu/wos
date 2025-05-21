package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 论文数据的实体类（暂时的取名，因为使用的数据是部分2022年的数据）
 */
@Entity
@Table(name = "main")
@Data
@Document(indexName = "main")
public class main2022 {
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public String getPubyear() {
        return pubyear;
    }

    public void setPubyear(String pubyear) {
        this.pubyear = pubyear;
    }

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
