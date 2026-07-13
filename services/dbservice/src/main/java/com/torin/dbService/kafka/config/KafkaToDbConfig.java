package com.torin.dbService.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.serialization.StringDeserializer;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KafkaToDbProperties.class)
public class KafkaToDbConfig {

    private final KafkaToDbProperties.Kafka kafkaProps;

    public KafkaToDbConfig(KafkaToDbProperties kafkaProps) {
        this.kafkaProps = kafkaProps.getKafka();
    }

    @Bean
    public ConsumerFactory<String, String> kafkaToDbConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProps.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                kafkaProps.getConsumer().getOrDefault("key-deserializer", StringDeserializer.class));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                kafkaProps.getConsumer().getOrDefault("value-deserializer", StringDeserializer.class));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProps.getConsumer().get("auto-offset-reset"));
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,
                Integer.parseInt(kafkaProps.getConsumer().getOrDefault("fetch-min-bytes", 5 * 1024 * 1024).toString()));
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG,
                Integer.parseInt(kafkaProps.getConsumer().getOrDefault("fetch-max-bytes", 3 * 1024 * 1024).toString()));
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                Integer.parseInt(kafkaProps.getConsumer().getOrDefault("max-partition-fetch-bytes", 5 * 1024 * 1024)
                        .toString()));
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                Integer.parseInt(kafkaProps.getConsumer().getOrDefault("max-poll-records", "100").toString()));
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,
                Integer.parseInt(kafkaProps.getConsumer().getOrDefault("fetch-max-wait-ms", "2000").toString()));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaToDbContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaToDbConsumerFactory());
        factory.setBatchListener(true);
        factory.getContainerProperties()
                .setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
        Long pollTimeout = 30000L;
        try {
            pollTimeout = Long
                    .valueOf(String.valueOf(kafkaProps.getListener().getOrDefault("poll-timeout", pollTimeout)));
        } catch (NumberFormatException ex) {
            System.out.println("Exception parse poll-timeout!");
        }
        FixedBackOff backOff = new FixedBackOff(2000L, FixedBackOff.UNLIMITED_ATTEMPTS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setPollTimeout(pollTimeout);
        return factory;
    }
}
