package com.torin.postgres.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import com.torin.postgres.config.R2dbcModuleConfig;

@AutoConfiguration
@Import(R2dbcModuleConfig.class)
public class PostgresPortsAutoConfiguration {

}
