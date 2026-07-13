package com.torin.dbService.jdbc.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.dbService.jdbc.adapter.BatchRepositoryAdapter;
import com.torin.dbService.jdbc.adapter.JdbcQueryAdapter;
import com.torin.dbService.jdbc.adapter.NoOpBatchRepositoryAdapter;
import com.torin.dbService.jdbc.adapter.NoOpJdbcQueryAdapter;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.jdbc.port.JdbcQueryPort;
import com.torin.dbService.jdbc.service.UserBatchService;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;

@Configuration
public class JdbcModuleConfig {
    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    JdbcQueryPort jdbcQueryAdapter(DataSource dataSource) {
        return new JdbcQueryAdapter(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    JdbcQueryPort noopJdbcQueryAdapter() {
        return new NoOpJdbcQueryAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    BatchRepositoryPort batchRepositoryAdapter(DataSource dataSource, WordGroupAllPort wordGroupAllPort,
            UserBatchService userBatchService) {
        return new BatchRepositoryAdapter(dataSource, wordGroupAllPort, userBatchService);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    BatchRepositoryPort noopBatchRepositoryAdapter() {
        return new NoOpBatchRepositoryAdapter();
    }
}
