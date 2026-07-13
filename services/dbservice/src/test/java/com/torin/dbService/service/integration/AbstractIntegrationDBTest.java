package com.torin.dbService.service.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationDBTest {
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16").withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        String host = postgres.getHost();
        int port = postgres.getMappedPort(5432);
        String dbName = postgres.getDatabaseName();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        registry.add("spring.datasource.jdbc-url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);

        registry.add("r2dbc.driver", () -> "postgresql");
        registry.add("r2dbc.host", () -> host);
        registry.add("r2dbc.port", () -> port);
        registry.add("r2dbc.db_name", () -> dbName);
        registry.add("r2dbc.username", () -> username);
        registry.add("r2dbc.password", () -> password);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }
}
