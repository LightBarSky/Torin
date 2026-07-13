package com.torin.es.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

import com.torin.es.config.EsConfig;

@AutoConfiguration
@ConditionalOnProperty(name = "es.enabled", havingValue = "true", matchIfMissing = true)
@Import(EsConfig.class)
public class ElasticsearchAutoconfiguration {
    
}
