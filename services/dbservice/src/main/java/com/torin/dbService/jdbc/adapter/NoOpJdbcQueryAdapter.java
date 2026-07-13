package com.torin.dbService.jdbc.adapter;

import java.util.List;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import com.torin.dbService.jdbc.port.JdbcQueryPort;

public class NoOpJdbcQueryAdapter implements JdbcQueryPort {

    @Override
    public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) {
        return List.of();
    }
    
}
