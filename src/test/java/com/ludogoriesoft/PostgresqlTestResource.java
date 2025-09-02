package com.ludogoriesoft;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresqlTestResource implements QuarkusTestResourceLifecycleManager {

    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Override
    public Map<String, String> start() {
        DATABASE.start();
        return Map.of(
                "quarkus.datasource.jdbc.url", DATABASE.getJdbcUrl(),
                "quarkus.datasource.username", DATABASE.getUsername(),
                "quarkus.datasource.password", DATABASE.getPassword()
        );
    }

    @Override
    public void stop() {
        DATABASE.stop();
    }
}