package com.torin.dbService.kafka.batch.buffer;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.support.Acknowledgment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;

public class BatchBuffer<T> {

    private final int batchSize;
    private final BatchProcessor<T> processor;
    private Acknowledgment ack = null;

    private final List<T> buffer = new ArrayList<>();
    private final Object lock = new Object();

    public BatchBuffer(int batchSize, BatchProcessor<T> processor) {
        this.batchSize = batchSize;
        this.processor = processor;
    }

    public Class<T> getType() {
        return processor.getType();
    }

    public void add(List<T> items, Instant lastMessageDate)
            throws SQLException, JsonProcessingException {
        List<T> batchToFlush = null;
        synchronized (lock) {
            buffer.addAll(items);
            if (buffer.size() >= batchSize) {
                batchToFlush = new ArrayList<>(buffer);
                buffer.clear();
            }
        }

        if (batchToFlush != null) {
            processor.process(batchToFlush, lastMessageDate);
            if (this.ack != null) {
                this.ack.acknowledge();
                this.ack = null;
            }
        }
    }

    public void setAcknowledgment(Acknowledgment ack) {
        this.ack = ack;
    }

    public void clear() {
        synchronized (lock) {
            buffer.clear();
        }
    }
}
