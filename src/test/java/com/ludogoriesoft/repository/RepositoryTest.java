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
    void testFindBySymbol_Found() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("RTST");
        assertTrue(foundCompany.isPresent());
        assertEquals("Repo Test Corp", foundCompany.get().getName());
    }

    @Test
    void testFindBySymbol_NotFound() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("NOSYMBOL");
        assertTrue(foundCompany.isEmpty());
    }

    // Tests for StockDataRepository

    @Test
    @Transactional
    void testFindLatestForToday_Success() {
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockData.setMarketCapitalization(100.0);
        stockDataRepository.persist(stockData);

        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);
        assertTrue(foundData.isPresent());
        assertEquals(100.0, foundData.get().getMarketCapitalization());
    }

    @Test
    @Transactional
    void testFindLatestForToday_IgnoresYesterdayData() {
        StockData stockData = new StockData();
        stockData.company = testCompany;
        stockDataRepository.persist(stockData);

        stockData.fetchedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        stockDataRepository.persist(stockData);

        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);
        assertTrue(foundData.isEmpty());
    }

    @Test
    @Transactional
    void testFindLatestForToday_ReturnsNewestEntry() {
        StockData oldStockData = new StockData();
        oldStockData.company = testCompany;
        oldStockData.setMarketCapitalization(100.0);
        stockDataRepository.persist(oldStockData);
        oldStockData.fetchedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        stockDataRepository.persist(oldStockData);

        StockData newStockData = new StockData();
        newStockData.company = testCompany;
        newStockData.setMarketCapitalization(200.0);
        stockDataRepository.persist(newStockData);

        Optional<StockData> foundData = stockDataRepository.findLatestByCompanyIdForToday(testCompany.id);

        assertTrue(foundData.isPresent());
        assertEquals(200.0, foundData.get().getMarketCapitalization());
    }
}