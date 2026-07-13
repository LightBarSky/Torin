package com.torin.postgres.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PgProperties.class)
@EnableR2dbcRepositories(
    basePackages = "com.torin.postgres.repository"
)
public class R2dbcRepositoryConfig {
    private final PgProperties pgProperties;

    public R2dbcRepositoryConfig(PgProperties pgProperties) {
        this.pgProperties = pgProperties;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(DRIVER, pgProperties.getDriver())
                        .option(HOST, pgProperties.getHost())
                        .option(USER, pgProperties.getUser())
                        .option(PASSWORD, pgProperties.getPassword())
                        .option(DATABASE, pgProperties.getDatabase())
                        .option(Option.valueOf("timeZone"), "UTC")
                        .build());
    }
}
