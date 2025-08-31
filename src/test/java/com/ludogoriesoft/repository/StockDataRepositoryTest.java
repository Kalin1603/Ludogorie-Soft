package com.ludogoriesoft.repository;

import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.entity.StockData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StockDataRepositoryTest {

    @Inject
    StockDataRepository stockDataRepository;

    @Inject
    CompanyRepository companyRepository;

    private Company testCompany;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clearing both repositories to start fresh
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();

        // Creating a parent company for the stock data
        Company company = new Company();
        company.setName("Stock Test Corp");
        company.setSymbol("STC");
        company.setCountry("UK");
        companyRepository.persistAndFlush(company);
        testCompany = company;
    }

    @AfterEach
    @Transactional
    void tearDown() {
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @Transactional
    void findLatestByCompanyIdForToday_shouldReturnData_whenPresent() {
        // ARRANGE
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockDataRepository.persist(stockData);

        // ACT
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        // ASSERT
        assertTrue(foundData.isPresent());
    }

    @Test
    @Transactional
    void findLatestByCompanyIdForToday_shouldIgnoreDataFromYesterday() {
        // ARRANGE: Creating the entity. Hibernate sets fetchedAt to NOW upon persist.
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockDataRepository.persist(stockData);
        stockDataRepository.flush(); // Force the INSERT to the database.

        // ACT: Explicitly UPDATING the timestamp to the past.
        stockData.fetchedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        stockDataRepository.persist(stockData);
        stockDataRepository.flush(); // Force the UPDATE to the database.

        // ASSERT: The query for today's data should correctly find nothing.
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);
        assertTrue(foundData.isEmpty(), "Should not find data from yesterday");
    }
}