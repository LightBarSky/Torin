package com.torin.analytic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.analytic.core.AnalyticCoreService;
import com.torin.analytic.service.AnalyticService;
import com.torin.analytic.service.IntermediateComputingService;
import com.torin.es.service.ElasticsearchService;
import com.torin.postgres.service.WordGroupAllService;

@Configuration(proxyBeanMethods = false)
public class AnalyticConfig {

    @Bean
    AnalyticCoreService analyticCoreService() {
        return new AnalyticCoreService();
    }

    @Bean
    IntermediateComputingService intermediateComputingService(AnalyticCoreService analyticCoreService) {
        return new IntermediateComputingService(analyticCoreService);
    }

    @Bean
    AnalyticService analyticService(ElasticsearchService elasticsearchService,
            IntermediateComputingService intermediateComputingService,
            WordGroupAllService wordGroupAllService) {
        return new AnalyticService(elasticsearchService, intermediateComputingService, wordGroupAllService);
    }
}
