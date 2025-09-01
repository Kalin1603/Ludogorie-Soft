package com.ludogoriesoft.service;

import com.ludogoriesoft.client.FinnhubClient;
import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.dto.FinnhubProfileDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.entity.StockData;
import com.ludogoriesoft.mapper.CompanyMapper;
import com.ludogoriesoft.repository.CompanyRepository;
import com.ludogoriesoft.repository.StockDataRepository;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Using Mockito runner for pure unit tests
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    // Create mock instances of all dependencies
    @Mock
    CompanyRepository companyRepository;
    @Mock
    CompanyMapper companyMapper;
    @Mock
    StockDataRepository stockDataRepository;
    @Mock
    FinnhubClient finnhubClient;

    // Inject the mocks into a real CompanyService instance
    @InjectMocks
    CompanyService companyService;

    // Re-initializing the service before each test to inject a value for the API key,
    // since @ConfigProperty doesn't work in a plain unit test.
    @BeforeEach
    void setUp() {
        companyService = new CompanyService(companyRepository, companyMapper, stockDataRepository, finnhubClient, "DUMMY_API_KEY");
    }

    @Test
    void createCompany_shouldSucceed_whenSymbolIsNew() {
        // ARRANGE
        CompanyDto dto = new CompanyDto(null, "NewCo", "DE", "NCO", null, null, null);
        Company companyEntity = new Company();
        when(companyRepository.findBySymbol("NCO")).thenReturn(Optional.empty());
        when(companyMapper.toEntity(dto)).thenReturn(companyEntity);

        // ACT
        companyService.createCompany(dto);

        // ASSERT
        verify(companyRepository).persist(companyEntity); // Verify that the company was saved
        verify(companyMapper).toDto(companyEntity); // Verify the result was mapped back
    }

    @Test
    void createCompany_shouldThrowConflict_whenSymbolExists() {
        // ARRANGE
        CompanyDto dto = new CompanyDto(null, "ExistingCo", "UK", "ECO", null, null, null);
        when(companyRepository.findBySymbol("ECO")).thenReturn(Optional.of(new Company()));

        // ACT & ASSERT
        assertThrows(WebApplicationException.class, () -> companyService.createCompany(dto));
    }

    @Test
    void getAllCompanies_shouldReturnMappedList() {
        // ARRANGE
        when(companyRepository.listAll()).thenReturn(List.of(new Company()));

        // ACT
        companyService.getAllCompanies();

        // ASSERT
        verify(companyRepository).listAll(); // Verify the repository was called
        verify(companyMapper, times(1)).toDto(any(Company.class)); // Verify the mapper was called for each item
    }

    @Test
    void updateCompany_shouldSucceed_whenIdExists() {
        // ARRANGE
        CompanyDto dto = new CompanyDto(1L, "Updated", "US", "UPD", null, null, null);
        Company existingCompany = new Company();
        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(existingCompany));

        // ACT
        companyService.updateCompany(1L, dto);

        // ASSERT
        verify(companyRepository).findByIdOptional(1L);
        verify(companyMapper).updateEntityFromDto(dto, existingCompany);
        verify(companyRepository).persist(existingCompany);
        verify(companyMapper).toDto(existingCompany);
    }

    @Test
    void updateCompany_shouldThrowNotFound_whenIdDoesNotExist() {
        // ARRANGE
        long nonExistentId = 99L;
        CompanyDto dummyDto = new CompanyDto(null, null, null, null, null, null, null);
        when(companyRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());

        // ACT & ASSERT: The lambda now has only one invocation.
        assertThrows(NotFoundException.class, () -> companyService.updateCompany(nonExistentId, dummyDto));
    }

    @Test
    void getCompanyStockData_shouldThrowNotFound_whenCompanyIdDoesNotExist() {
        // ARRANGE
        when(companyRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(NotFoundException.class, () -> companyService.getCompanyStockData(99L));
    }

    @Test
    void getCompanyStockData_shouldReturnFromCache_whenCacheIsPresent() {
        // ARRANGE (Cache Hit)
        Company company = new Company();
        StockData cachedData = new StockData();
        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(company));
        when(stockDataRepository.findLatestByCompanyIdForToday(1L)).thenReturn(Optional.of(cachedData));

        // ACT
        companyService.getCompanyStockData(1L);

        // ASSERT
        verify(finnhubClient, never()).getCompanyProfile(anyString(), anyString()); // Verify the external API was NOT called
        verify(companyMapper).toCompanyStockDto(company, cachedData); // Verify we mapped the cached data
    }

    @Test
    void getCompanyStockData_shouldFetchFromApi_whenCacheIsMissing() {
        // ARRANGE (Cache Miss)
        Company company = new Company();
        company.setSymbol("API");
        FinnhubProfileDto apiResponse = new FinnhubProfileDto(500.0, 200.0, "Some Name", "Some Country", "Some Symbol");
        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(company));
        when(stockDataRepository.findLatestByCompanyIdForToday(1L)).thenReturn(Optional.empty());
        when(finnhubClient.getCompanyProfile(eq("API"), anyString())).thenReturn(apiResponse);

        // ACT
        companyService.getCompanyStockData(1L);

        // ASSERT
        verify(finnhubClient).getCompanyProfile("API", "DUMMY_API_KEY"); // Verify the external API WAS called
        verify(stockDataRepository).persist(any(StockData.class)); // Verify that new stock data was saved
        verify(companyMapper).toCompanyStockDto(any(Company.class), any(StockData.class));
    }
}