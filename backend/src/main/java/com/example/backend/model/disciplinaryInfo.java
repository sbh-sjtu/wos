package com.example.backend.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "disciplinaryinfo")
public class disciplinaryInfo {
    @org.springframework.data.annotation.Id
    private Integer seq_temp;
    private String wos_uid;
    private String nlp_keyword;

    // 由于使用了@Data注解，手动的getter/setter方法可以删除
}