package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Entity
@Table(name = "dsiciplinaryInfo")
@Data
@Document(indexName = "disciplinaryinfo")
public class disciplinaryInfo {
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer seq_temp;
    private String wos_uid;
    private String nlp_keyword;

    public Integer getSeq_temp() {
        return seq_temp;
    }

    public void setSeq_temp(Integer seq_temp) {
        this.seq_temp = seq_temp;
    }

    public String getWos_uid() {
        return wos_uid;
    }

    public void setWos_uid(String wos_uid) {
        this.wos_uid = wos_uid;
    }

    public String getNlp_keyword() {
        return nlp_keyword;
    }

    public void setNlp_keyword(String nlp_keyword) {
        this.nlp_keyword = nlp_keyword;
    }
}
