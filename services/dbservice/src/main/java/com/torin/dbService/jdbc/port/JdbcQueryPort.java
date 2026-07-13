package com.torin.dbService.jdbc.port;

import java.util.List;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

public interface JdbcQueryPort {
    <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper);
}
