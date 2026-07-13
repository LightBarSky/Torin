package com.torin.dbService.elasticsearch.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.dbService.elasticsearch.adapter.LogHandlerAdapter;
import com.torin.dbService.elasticsearch.adapter.NoOpLogHandlerAdapter;
import com.torin.dbService.elasticsearch.port.LogHandlerPort;
import com.torin.dbService.elasticsearch.repository.LogHandlerRepository;

@Configuration
public class ElasticSearchModuleConfiguration {
    @Bean
    @ConditionalOnProperty(name = "es.enabled", havingValue = "true")
    public LogHandlerPort elasticAdapter(LogHandlerRepository repository) {
        return new LogHandlerAdapter(repository);
    }

    @Bean
    @ConditionalOnMissingBean(LogHandlerPort.class)
    public LogHandlerPort noopAdapter() {
        return new NoOpLogHandlerAdapter();
    }
}
