package com.torin.dbService.jdbc.adapter;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import com.torin.dbService.jdbc.port.JdbcQueryPort;

public class JdbcQueryAdapter implements JdbcQueryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcQueryAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(psc, rowMapper);
    }

}
