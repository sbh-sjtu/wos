package com.example.backend.service.impl;

import com.example.backend.model.main2022;
import com.example.backend.service.DownloadService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DownloadServiceImpl implements DownloadService {

    @Override
    public ResponseEntity<byte[]> downloadCSV(List<main2022> data) {
        String[] header = {"seq_temp","wos_uid","database","sortdate","pubyear","has_abstract","coverdate","pubmonth","vol","issue",
        "special_issue","supplement","early_access_date","early_access_month","early_access_year","article_type","page_count",
        "page_begin","page_end","journal_title_source","journal_title_abbrev","journal_title_iso","journal_title_11","journal_title_29",
        "article_title","article_doctype","heading","subheadings","subject_traditional","subject_extended","fund_text","keyword",
        "keyword_plus","abstract_text","ids","bib_id","bib_pagecount","reviewed_work","languages","rw_authors","rw_year","rw_language",
        "book_note","bk_binding","bk_publisher","bk_prepay","bk_ordering","identifier_accession_no","identifier_issn","identifier_eissn",
        "identifier_isbn","identifier_eisbn","identifier_doi","identifier_pmid","normalized_doctype","is_OA","oases","subj_group_macro_id",
        "subj_group_macro_value","subj_group_meso_id","subj_group_meso_value","subj_group_micro_id","subj_group_micro_value",
        "author_fullname","author_displayname","author_wosname","grant_info","address","reprint_address","email","contributor",
                "publisher","publisher_unified","publisher_display"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // 写入表头
            outputStream.write(String.join(",", header).getBytes(StandardCharsets.UTF_8));
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));

            // 写入每一行数据
            for (main2022 main2022 : data) {
                String[] row = {
                        String.valueOf(main2022.getSeq_temp()),
                        main2022.getWos_uid(),
                        main2022.getDatabase(),
                        main2022.getSortdate(),
                        main2022.getPubyear(),
                        main2022.getHas_abstract(),
                        main2022.getCoverdate(),
                        main2022.getPubmonth(),
                        main2022.getVol(),
                        main2022.getIssue(),
                        main2022.getSpecial_issue(),
                        main2022.getSupplement(),
                        main2022.getEarly_access_date(),
                        main2022.getEarly_access_month(),
                        main2022.getEarly_access_year(),
                        main2022.getArticle_type(),
                        main2022.getPage_count(),
                        main2022.getPage_begin(),
                        main2022.getPage_end(),
                        main2022.getJournal_title_source(),
                        main2022.getJournal_title_abbrev(),
                        main2022.getJournal_title_iso(),
                        main2022.getJournal_title_11(),
                        main2022.getJournal_title_29(),
                        main2022.getArticle_title(),
                        main2022.getArticle_doctype(),
                        main2022.getHeading(),
                        main2022.getSubheadings(),
                        main2022.getSubject_traditional(),
                        main2022.getSubject_extended(),
                        main2022.getFund_text(),
                        main2022.getKeyword(),
                        main2022.getKeyword_plus(),
                        main2022.getAbstract_text(),
                        main2022.getIds(),
                        main2022.getBib_id(),
                        main2022.getBib_pagecount(),
                        main2022.getReviewed_work(),
                        main2022.getLanguages(),
                        main2022.getRw_authors(),
                        main2022.getRw_year(),
                        main2022.getRw_language(),
                        main2022.getBook_note(),
                        main2022.getBk_binding(),
                        main2022.getBk_publisher(),
                        main2022.getBk_prepay(),
                        main2022.getBk_ordering(),
                        main2022.getIdentifier_accession_no(),
                        main2022.getIdentifier_issn(),
                        main2022.getIdentifier_eissn(),
                        main2022.getIdentifier_isbn(),
                        main2022.getIdentifier_eisbn(),
                        main2022.getIdentifier_doi(),
                        main2022.getIdentifier_pmid(),
                        main2022.getNormalized_doctype(),
                        main2022.getIs_OA(),
                        main2022.getOases(),
                        main2022.getSubj_group_macro_id(),
                        main2022.getSubj_group_macro_value(),
                        main2022.getSubj_group_meso_id(),
                        main2022.getSubj_group_meso_value(),
                        main2022.getSubj_group_micro_id(),
                        main2022.getSubj_group_micro_value(),
                        main2022.getAuthor_fullname(),
                        main2022.getAuthor_displayname(),
                        main2022.getAuthor_wosname(),
                        main2022.getGrant_info(),
                        main2022.getAddress(),
                        main2022.getReprint_address(),
                        main2022.getEmail(),
                        main2022.getContributor(),
                        main2022.getPublisher(),
                        main2022.getPublisher_unified(),
                        main2022.getPublisher_display()
                };

                // 使用引号包裹包含逗号或换行符的字段
                String csvRow = Arrays.stream(row)
                        .map(field -> {
                            if (field != null && (field.contains(",") || field.contains("\"") || field.contains("\n"))) {
                                field = field.replace("\"", "\"\"");  // 转义双引号
                                return "\"" + field + "\"";  // 包裹字段
                            }
                            return field;
                        })
                        .collect(Collectors.joining(","));  // 拼接为CSV行

                outputStream.write(csvRow.getBytes(StandardCharsets.UTF_8));
                outputStream.write("\n".getBytes(StandardCharsets.UTF_8));

            }

            // 返回 CSV 文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper_detail.csv")
                    .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
