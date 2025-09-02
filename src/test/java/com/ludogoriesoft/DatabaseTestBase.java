package com.ludogoriesoft;

import io.quarkus.test.common.QuarkusTestResource;

/**
 * Base class for all integration tests that require a database.
 * By annotating this class with {@link QuarkusTestResource}, any test class
 * that extends this base class will automatically have the PostgreSQL
 * Testcontainer started before its tests are run.
 */
@QuarkusTestResource(PostgresqlTestResource.class)
public abstract class DatabaseTestBase {
    // This class is intentionally left empty.
    // Its purpose is to hold the @QuarkusTestResource annotation so we don't have to repeat it on every single integration test class.
}