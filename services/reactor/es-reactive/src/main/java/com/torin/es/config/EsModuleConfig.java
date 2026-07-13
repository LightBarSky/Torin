package com.torin.es.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;

import com.torin.es.adapter.ElasticSearchAdapter;
import com.torin.es.adapter.NoOpElasticSearchAdadpter;
import com.torin.es.port.ElasticSearchPort;
import com.torin.es.service.ElasticsearchService;

@Configuration(proxyBeanMethods = false)
public class EsModuleConfig {

    @Bean
    @ConditionalOnProperty(name = "es.enabled", havingValue = "true", matchIfMissing = true)
    public ElasticSearchPort elasticSearchAdapter(ReactiveElasticsearchClient reactiveElasticsearchClient) {
        return new ElasticSearchAdapter(reactiveElasticsearchClient);
    }

    @Bean
    @ConditionalOnProperty(name = "es.enabled", havingValue = "false")
    public ElasticSearchPort noopElasticSearchAdapter() {
        return new NoOpElasticSearchAdadpter();
    }

    @Bean
    ElasticsearchService elasticsearchService(ElasticSearchPort elasticSearchPort) {
        return new ElasticsearchService(elasticSearchPort);
    }
}
