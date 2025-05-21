package com.example.backend.service.impl;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class NLPService {
    private final StanfordCoreNLP pipeline;

    public NLPService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        pipeline = new StanfordCoreNLP(props);
    }

    public List<String> tokenize(String text) {
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);
        List<String> tokens = new ArrayList<>();
        document.tokens().forEach(token -> tokens.add(token.word()));
        return tokens;
    }

}
