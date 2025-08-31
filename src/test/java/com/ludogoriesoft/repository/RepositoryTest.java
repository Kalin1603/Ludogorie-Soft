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

@QuarkusTest // Using the Quarkus test runner to get a real database connection
class RepositoryTest {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    StockDataRepository stockDataRepository;

    private Company testCompany;

    @BeforeEach
    @Transactional // Using transactions to manage database setup
    void setUp() {
        // Cleaning database before each test
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();

        // Creating a predictable company for tests
        Company company = new Company();
        company.setName("Repo Test Corp");
        company.setCountry("RT");
        company.setSymbol("RTST");
        companyRepository.persist(company);
        testCompany = company;
    }

    @AfterEach
    @Transactional
    void tearDown() {
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();
    }

    // Tests for CompanyRepository

    @Test
    void findBySymbol_shouldReturnCompany_whenSymbolExists() {
        // ACT: Calling the custom query method
        Optional<Company> foundCompany = companyRepository.findBySymbol("RTST");

        // ASSERT: Verifying that the correct company was found
        assertTrue(foundCompany.isPresent());
        assertEquals("Repo Test Corp", foundCompany.get().getName());
    }

    @Test
    void findBySymbol_shouldReturnEmpty_whenSymbolDoesNotExist() {
        // ACT: Calling the custom query with a symbol that doesn't exist
        Optional<Company> foundCompany = companyRepository.findBySymbol("NOSYMBOL");

        // ASSERT: Verifying that nothing was found
        assertTrue(foundCompany.isEmpty());
    }

    // Tests for StockDataRepository

    @Test
    @Transactional
    void findLatestByCompanyIdForToday_shouldReturnData_whenPresentForToday() {
        // ARRANGE: Create stock data that was fetched today
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockData.setMarketCapitalization(100.0);
        stockDataRepository.persist(stockData);

        // ACT: Calling the custom query
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        // ASSERT: Verifying the data was found
        assertTrue(foundData.isPresent());
        assertEquals(100.0, foundData.get().getMarketCapitalization());
    }

    @Test
    @Transactional
    void findLatestByCompanyIdForToday_shouldReturnEmpty_whenDataIsFromYesterday() {
        // ARRANGE: Creating stock data that was fetched yesterday
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockData.fetchedAt = Instant.now().minus(1, ChronoUnit.DAYS); // Set timestamp to yesterday
        stockDataRepository.persist(stockData);

        // ACT: Calling the custom query
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        // ASSERT: Verifying that nothing was found for today
        assertTrue(foundData.isEmpty());
    }

    @Test
    @Transactional
    void findLatestByCompanyIdForToday_shouldReturnLatest_whenMultipleEntriesExistForToday() {
        // ARRANGE: Creating two entries for today, one older and one newer
        StockData oldStockData = new StockData();
        oldStockData.company = testCompany;
        oldStockData.fetchedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        oldStockData.setMarketCapitalization(100.0);
        stockDataRepository.persist(oldStockData);

        StockData newStockData = new StockData();
        newStockData.company = testCompany;
        newStockData.fetchedAt = Instant.now();
        newStockData.setMarketCapitalization(200.0); // The newest data
        stockDataRepository.persist(newStockData);

        // ACT: Calling the custom query
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        // ASSERT: Verifying the newest data was returned
        assertTrue(foundData.isPresent());
        assertEquals(200.0, foundData.get().getMarketCapitalization());
    }
}