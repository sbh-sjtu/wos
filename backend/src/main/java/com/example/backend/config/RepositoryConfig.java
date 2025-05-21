package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@EnableElasticsearchRepositories(basePackages = "com.example.backend.repository")
@Configuration
public class RepositoryConfig {
}

