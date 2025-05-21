package com.example.backend.repository;
import com.example.backend.model.main2022;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Main2022ElasticSearchRepository extends ElasticsearchRepository<main2022, Integer> {
}