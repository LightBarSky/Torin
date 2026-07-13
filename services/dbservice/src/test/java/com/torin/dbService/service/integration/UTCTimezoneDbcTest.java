package com.torin.dbService.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
public class UTCTimezoneDbcTest extends AbstractIntegrationDBTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private R2dbcEntityTemplate r2dbcTemplate;

    @Test
    void jdbcShouldBeUtc() throws SQLException {
        String tz = jdbcTemplate.queryForObject("SHOW TIMEZONE", String.class);
        assertEquals(tz, "UTC");
    }

    @Test
    void r2dbcShouldBeUtc() {
        StepVerifier.create(
                r2dbcTemplate.getDatabaseClient()
                        .sql("SHOW TIMEZONE")
                        .map(row -> row.get(0, String.class))
                        .one())
                .assertNext(tz -> assertEquals("UTC", tz))
                .verifyComplete();
    }
}
