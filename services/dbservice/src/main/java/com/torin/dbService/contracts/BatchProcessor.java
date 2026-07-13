package com.torin.dbService.contracts;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface BatchProcessor<T> {
    void process(List<T> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException;
    Class<T> getType();
}