package com.torin.dbService.r2dbc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(R2dbcProperties.class)
@EnableR2dbcRepositories(basePackages = "com.torin.dbService.r2dbc.repository")
public class R2dbcRepositoryConfig {

    private final R2dbcProperties r2dbcProperties;

    public R2dbcRepositoryConfig(R2dbcProperties r2dbcProperties) {
        this.r2dbcProperties = r2dbcProperties;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(DRIVER, r2dbcProperties.getDriver())
                        .option(HOST, r2dbcProperties.getHost())
                        .option(PORT, r2dbcProperties.getPort())
                        .option(USER, r2dbcProperties.getUsername())
                        .option(PASSWORD, r2dbcProperties.getPassword())
                        .option(DATABASE, r2dbcProperties.getDbName())
                        .option(Option.valueOf("timeZone"), "UTC")
                        .build());
    }

    @Bean(name = "r2dbcTransactionManager")
    public ReactiveTransactionManager r2dbcTransactionManager(
            ConnectionFactory connectionFactory) {

        return new R2dbcTransactionManager(connectionFactory);
    }
}
