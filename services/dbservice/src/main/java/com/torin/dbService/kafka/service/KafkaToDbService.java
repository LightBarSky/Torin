package com.torin.dbService.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.torin.dbService.dto.KafkaObjectDto;
import com.torin.dbService.kafka.batch.buffer.BatchBuffer;
import com.torin.dbService.kafka.batch.buffer.BatchBufferRegistry;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class KafkaToDbService {
    private final KafkaListenerEndpointRegistry registry;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private final BatchBufferRegistry batchBufferRegistry;
    private final ObjectReader kafkaObjectReader;
    private final ObjectMapper objectMapper;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public KafkaToDbService(
            KafkaListenerEndpointRegistry registry,
            ObjectMapper objectMapper,
            BatchBufferRegistry batchBufferRegistry,
            SendLogKafkaToDbService sendLogKafkaToDbService) {

        this.registry = registry;
        this.batchBufferRegistry = batchBufferRegistry;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;

        this.kafkaObjectReader = objectMapper.readerFor(KafkaObjectDto.class);
        this.objectMapper = objectMapper;
    }

    public void stopListeners() throws InterruptedException, JsonProcessingException {
        atomicBoolean.set(false);
        registry.getListenerContainersMatching(id -> id.startsWith("kafkaToDb"))
                .forEach(MessageListenerContainer::stop);
        batchBufferRegistry.clearAll();

        sendLogKafkaToDbService.clearStatus();
        sendLogKafkaToDbService.sendLog("Слушатели остановлены", "Info");
    }

    public void startListeners() throws JsonProcessingException {
        atomicBoolean.set(true);
        registry.getListenerContainersMatching(id -> id.startsWith("kafkaToDb"))
                .forEach(MessageListenerContainer::start);
        sendLogKafkaToDbService.sendLog("Слушатели запущены", "Info");
    }

    public Boolean getRunning() {
        return atomicBoolean.get();
    }

    public <T> void process(List<ConsumerRecord<String, String>> records, Acknowledgment ack,
            Class<T> clazz)
            throws Exception {
        if (!atomicBoolean.get()) {
            return;
        }
        try {
            BatchBuffer<T> buffer = batchBufferRegistry.get(clazz);
            CollectionType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            for (ConsumerRecord<String, String> record : records) {
                KafkaObjectDto kafkaObject = kafkaObjectReader.readValue(record.value());
                List<T> parsed = objectMapper.readValue(kafkaObject.serializeObjects(),
                        type);
                if (parsed != null && !parsed.isEmpty()) {
                    buffer.add(parsed, Instant.ofEpochMilli(record.timestamp()));
                }
            }
            buffer.setAcknowledgment(ack);
        } catch (SQLException se) {
            sendLogKafkaToDbService.sendLog("SQLException: " + se, "Error");
            throw new SQLException();
        } catch (Exception e) {
            sendLogKafkaToDbService.sendLog("Exception: " + e, "Error");
            throw new Exception();
        }
    }
}
