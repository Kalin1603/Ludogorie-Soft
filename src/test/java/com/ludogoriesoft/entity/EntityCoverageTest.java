package com.ludogoriesoft.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

// This is a plain JUnit test class. It does not need the Quarkus test runner.
class EntityCoverageTest {

    @Test
    void testCompanyEntityGettersAndSetters() {
        // Create an instance of the Company entity
        Company company = new Company();

        // Set all properties using the setters
        company.setName("Test Name");
        company.setCountry("TC");
        company.setSymbol("TSYM");
        company.setWebsite("http://test.com");
        company.setEmail("test@test.com");

        // Assert that all getters return the correct values
        assertEquals("Test Name", company.getName());
        assertEquals("TC", company.getCountry());
        assertEquals("TSYM", company.getSymbol());
        assertEquals("http://test.com", company.getWebsite());
        assertEquals("test@test.com", company.getEmail());

        // Testing its getter if it's not null
        assertNull(company.getCreatedAt()); // It will be null until persisted
    }

    @Test
    void testStockDataEntityGettersAndSetters() {
        // Create instances of StockData and its related Company
        StockData stockData = new StockData();
        Company company = new Company();
        Instant now = Instant.now();

        // Set all properties using the setters
        stockData.setMarketCapitalization(1000.50);
        stockData.setShareOutstanding(500.25);
        stockData.company = company; // Public field access
        stockData.fetchedAt = now; // Public field access

        // Assert that all getters and public fields return the correct values
        assertEquals(1000.50, stockData.getMarketCapitalization());
        assertEquals(500.25, stockData.getShareOutstanding());
        assertSame(company, stockData.company);
        assertSame(now, stockData.fetchedAt);
    }
}