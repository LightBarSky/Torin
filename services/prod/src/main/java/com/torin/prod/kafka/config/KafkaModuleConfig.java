package com.torin.prod.kafka.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.torin.prod.kafka.adapter.KafkaSendAdapter;
import com.torin.prod.kafka.adapter.NoOpKafkaReceiver;
import com.torin.prod.kafka.adapter.NoOpKafkaSendAdapter;
import com.torin.prod.kafka.config.KafkaConfig.Topic;
import com.torin.prod.kafka.port.KafkaSendPort;

import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Configuration
@EnableConfigurationProperties(KafkaConfig.class)
public class KafkaModuleConfig {
    private final KafkaConfig kafkaConfig;

    public KafkaModuleConfig(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

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

    @Bean("logsReceiver")
    @ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
    public KafkaReceiver<String, String> logsReceiver() {
        return createReceiver(kafkaConfig.getTopics().getLogs());
    }

    @Bean("logsReceiver")
    @ConditionalOnProperty(name = "kafka.enabled", havingValue = "false")
    public KafkaReceiver<String, String> noopLogsReceiver() {
        return new NoOpKafkaReceiver<>();
    }

    @Bean("statusReceiver")
    @ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
    public KafkaReceiver<String, String> statusReceiver() {
        return createReceiver(kafkaConfig.getTopics().getStatus());
    }

    @Bean("statusReceiver")
    @ConditionalOnProperty(name = "kafka.enabled", havingValue = "false")
    public KafkaReceiver<String, String> noopStatusReceiver() {
        return new NoOpKafkaReceiver<>();
    }

    private KafkaReceiver<String, String> createReceiver(Topic topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, topic.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfig.getConsumer().getAutoOffsetReset());

        ReceiverOptions<String, String> options = ReceiverOptions.<String, String>create(props)
                .subscription(List.of(topic.getName()));

        return KafkaReceiver.create(options);
    }
}
