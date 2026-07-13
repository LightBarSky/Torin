package com.torin.dbService.kafka.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.torin.dbService.kafka.adapter.KafkaSendAdapter;
import com.torin.dbService.kafka.adapter.NoOpKafkaSendAdapter;
import com.torin.dbService.kafka.port.KafkaSendPort;

@Configuration
public class KafkaModuleConfiguration {
    @Bean
    @ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
    public KafkaSendPort kafkaSendAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaSendAdapter(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(KafkaSendPort.class)
    public KafkaSendPort noopKafkaSendAdapter() {
        return new NoOpKafkaSendAdapter();
    }
}
