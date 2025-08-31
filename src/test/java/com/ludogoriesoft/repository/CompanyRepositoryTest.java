package com.ludogoriesoft.repository;

import com.ludogoriesoft.entity.Company;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CompanyRepositoryTest {

    @Inject
    CompanyRepository companyRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        companyRepository.deleteAll();
        Company company = new Company();
        company.setName("Test Corp");
        company.setCountry("US");
        company.setSymbol("TC");
        companyRepository.persist(company);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        companyRepository.deleteAll();
    }

    @Test
    void findBySymbol_shouldReturnCompany_whenSymbolExists() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("TC");
        assertTrue(foundCompany.isPresent());
        assertEquals("Test Corp", foundCompany.get().getName());
    }

    @Test
    void findBySymbol_shouldReturnEmpty_whenSymbolDoesNotExist() {
        Optional<Company> foundCompany = companyRepository.findBySymbol("NOSYMBOL");
        assertTrue(foundCompany.isEmpty());
    }
}