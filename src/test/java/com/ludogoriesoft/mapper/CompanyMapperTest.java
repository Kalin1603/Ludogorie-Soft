package com.ludogoriesoft.mapper;

import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.dto.CompanyStockDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.entity.StockData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyMapperTest {

    private final CompanyMapper companyMapper = new CompanyMapper();

    @Test
    void testToEntity_shouldMapAllFields() {
        // ARRANGE: Create a DTO with sample data
        CompanyDto dto = new CompanyDto(null, "Test Company", "US", "TCKR", "http://test.com", "test@test.com", null);

        // ACT: Call the method to be tested
        Company entity = companyMapper.toEntity(dto);

        // ASSERT: Verify that every field was mapped correctly
        assertNotNull(entity);
        assertEquals("Test Company", entity.getName());
        assertEquals("US", entity.getCountry());
        assertEquals("TCKR", entity.getSymbol());
        assertEquals("http://test.com", entity.getWebsite());
        assertEquals("test@test.com", entity.getEmail());
    }

    @Test
    void testToEntity_shouldReturnNull_whenDtoIsNull() {
        // ACT & ASSERT: Verify the null check
        assertNull(companyMapper.toEntity(null));
    }

    @Test
    void testToDto_shouldMapAllFields() {
        // ARRANGE
        Company entity = new Company();
        entity.id = 1L; // id is public from PanacheEntity
        entity.setName("Test Entity");
        entity.setCountry("DE");
        entity.setSymbol("TENT");
        entity.setWebsite("http://entity.com");
        entity.setEmail("entity@test.com");
        // Can't set createdAt, but can check it's passed through if it's there

        // ACT
        CompanyDto dto = companyMapper.toDto(entity);

        // ASSERT
        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Test Entity", dto.name());
        assertEquals("DE", dto.country());
        assertEquals("TENT", dto.symbol());
        assertEquals("http://entity.com", dto.website());
        assertEquals("entity@test.com", dto.email());
    }

    @Test
    void testToDto_shouldReturnNull_whenEntityIsNull() {
        // ACT & ASSERT: Verify the null check
        assertNull(companyMapper.toDto(null));
    }

    @Test
    void testUpdateEntityFromDto_shouldUpdateAllFields() {
        // ARRANGE
        Company entity = new Company();
        entity.setName("Old Name");
        entity.setCountry("FR");

        CompanyDto dto = new CompanyDto(null, "New Name", "JP", "NEWSYM", "http://new.com", "new@test.com", null);

        // ACT: Calling the update method
        companyMapper.updateEntityFromDto(dto, entity);

        // ASSERT
        assertEquals("New Name", entity.getName());
        assertEquals("JP", entity.getCountry());
        assertEquals("NEWSYM", entity.getSymbol());
        assertEquals("http://new.com", entity.getWebsite());
        assertEquals("new@test.com", entity.getEmail());
    }

    @Test
    void testUpdateEntityFromDto_shouldDoNothing_whenDtoIsNull() {
        // ARRANGE
        Company entity = new Company();
        entity.setName("Original Name");

        // ACT
        companyMapper.updateEntityFromDto(null, entity);

        // ASSERT
        assertEquals("Original Name", entity.getName());
    }

    @Test
    void toCompanyStockDto_shouldCombineData() {
        // ARRANGE: Creating sample Company and StockData
        Company company = new Company();
        company.id = 1L;
        company.setName("Stock Corp");
        company.setCountry("UK");
        company.setSymbol("STCK");

        StockData stockData = new StockData();
        stockData.setMarketCapitalization(5000.0);
        stockData.setShareOutstanding(1234.0);

        // ACT: Calling the mapping method
        CompanyStockDto result = companyMapper.toCompanyStockDto(company, stockData);

        // ASSERT: Verify the combined DTO contains all the correct data
        assertEquals(1L, result.id());
        assertEquals("Stock Corp", result.name());
        assertEquals("UK", result.symbol());
        assertEquals(5000.0, result.marketCapitalization());
        assertEquals(1234.0, result.shareOutstanding());
    }
}