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
class RepositoryTest {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    StockDataRepository stockDataRepository;

    private Company testCompany;

    @BeforeEach
    @Transactional
    void setUp() {
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();

        Company company = new Company();
        company.setName("Repo Test Corp");
        company.setCountry("RT");
        company.setSymbol("RTST");
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
    void testFindBySymbol_Found() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("RTST");
        assertTrue(foundCompany.isPresent());
    }

    @Test
    void testFindBySymbol_NotFound() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("NOSYMBOL");
        assertTrue(foundCompany.isEmpty());
    }

    @Test
    @Transactional
    void testFindLatestForToday_Success() {
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockDataRepository.persist(stockData);

        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);
        assertTrue(foundData.isPresent());
    }

    @Test
    @Transactional
    void testFindLatestForToday_IgnoresYesterdayData() {
        // ARRANGE: Persist the entity. Hibernate sets fetchedAt to NOW.
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockDataRepository.persist(stockData);
        // Force the INSERT to be written to the database immediately.
        stockDataRepository.flush();

        // ACT: Updating the entity's timestamp and re-persist (which schedules an UPDATE).
        stockData.fetchedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        stockDataRepository.persist(stockData);
        // Force the UPDATE to be written to the database immediately.
        stockDataRepository.flush();

        // ASSERT: The query now runs against the correct and final database state.
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);
        assertTrue(foundData.isEmpty(), "Should not find data from yesterday when querying for today");
    }

    @Test
    @Transactional
    void testFindLatestForToday_ReturnsNewestEntry() {
        // ARRANGE: Create an older entry and explicitly set its time back
        StockData oldStockData = new StockData();
        oldStockData.company = testCompany;
        stockDataRepository.persist(oldStockData);
        stockDataRepository.flush(); // Flush to DB
        oldStockData.fetchedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        stockDataRepository.persist(oldStockData);
        stockDataRepository.flush(); // Flush the update

        // ARRANGE: Create a newer entry
        StockData newStockData = new StockData();
        newStockData.company = testCompany;
        newStockData.setMarketCapitalization(200.0);
        stockDataRepository.persistAndFlush(newStockData); // Persist and flush in one step

        // ACT
        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        // ASSERT
        assertTrue(foundData.isPresent());
        assertEquals(200.0, foundData.get().getMarketCapitalization());
    }
}