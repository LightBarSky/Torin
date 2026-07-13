package com.torin.dbService.kafka.batch.buffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class BatchBufferRegistry {
    private final Map<Class<?>, BatchBuffer<?>> buffers = new ConcurrentHashMap<>();

    public <T> void register(Class<T> clazz, BatchBuffer<T> buffer) {
        buffers.put(clazz, buffer);
    }

    @SuppressWarnings("unchecked")
    public <T> BatchBuffer<T> get(Class<T> clazz) {
        BatchBuffer<?> buffer = buffers.get(clazz);

        if (buffer == null) {
            throw new IllegalStateException("No buffer for " + clazz);
        }

        if (!buffer.getType().equals(clazz)) {
            throw new IllegalStateException("Mismatch type for " + clazz);
        }

        return (BatchBuffer<T>) buffer;
    }

    public void clearAll() {
        buffers.values().forEach(BatchBuffer::clear);
    }
}
