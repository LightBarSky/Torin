package com.torin.postgres.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

import com.torin.postgres.config.R2dbcRepositoryConfig;

@AutoConfiguration
@ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
@Import(R2dbcRepositoryConfig.class)
public class PostgresAutoConfiguration {

}
