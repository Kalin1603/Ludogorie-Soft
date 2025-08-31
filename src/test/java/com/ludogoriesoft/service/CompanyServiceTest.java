package com.ludogoriesoft.service;

import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.mapper.CompanyMapper;
import com.ludogoriesoft.repository.CompanyRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class CompanyServiceTest {

    @Inject
    CompanyService companyService;


    @InjectMock
    CompanyRepository companyRepository;

    @InjectMock
    CompanyMapper companyMapper;

    @Test
    void getAllCompanies_shouldReturnListOfCompanies() {
        // ARRANGE
        Company company = new Company(); // Assuming setters are available now
        company.setName("Test Corp");
        List<Company> companies = List.of(company);
        CompanyDto dto = new CompanyDto(1L, "Test Corp", "US", "TC", null, null, Instant.now());

        when(companyRepository.listAll()).thenReturn(companies);
        when(companyMapper.toDto(any(Company.class))).thenReturn(dto);

        // ACT
        List<CompanyDto> result = companyService.getAllCompanies();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Corp", result.get(0).name());
    }

    @Test
    void createCompany_whenSymbolDoesNotExist_shouldCreateCompany() {
        // ARRANGE
        CompanyDto requestDto = new CompanyDto(null, "NewCo", "DE", "NCO", null, null, null);
        Company companyEntity = new Company();
        CompanyDto responseDto = new CompanyDto(1L, "NewCo", "DE", "NCO", null, null, Instant.now());

        when(companyRepository.findBySymbol("NCO")).thenReturn(Optional.empty());
        when(companyMapper.toEntity(requestDto)).thenReturn(companyEntity);
        when(companyMapper.toDto(companyEntity)).thenReturn(responseDto);

        // ACT
        CompanyDto result = companyService.createCompany(requestDto);

        // ASSERT
        assertNotNull(result);
        assertEquals("NewCo", result.name());
        verify(companyRepository).persist(companyEntity);
    }

    @Test
    void createCompany_whenSymbolExists_shouldThrowConflictException() {
        // ARRANGE
        CompanyDto requestDto = new CompanyDto(null, "ExistingCo", "UK", "ECO", null, null, null);
        when(companyRepository.findBySymbol("ECO")).thenReturn(Optional.of(new Company()));

        // ACT & ASSERT
        // Assert that executing this code throws the expected exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            companyService.createCompany(requestDto);
        });

        assertEquals(409, exception.getResponse().getStatus()); // Check for HTTP 409 Conflict
    }

    @Test
    void updateCompany_whenCompanyExists_shouldUpdateAndReturnDto() {
        // ARRANGE
        long companyId = 1L;
        CompanyDto requestDto = new CompanyDto(companyId, "Updated Name", "FR", "UCO", null, null, null);
        Company existingCompany = new Company();

        when(companyRepository.findByIdOptional(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyMapper.toDto(existingCompany)).thenReturn(requestDto);

        // ACT
        CompanyDto result = companyService.updateCompany(companyId, requestDto);

        // ASSERT
        assertNotNull(result);
        assertEquals("Updated Name", result.name());
        verify(companyMapper).updateEntityFromDto(requestDto, existingCompany);
        verify(companyRepository).persist(existingCompany);
    }

    @Test
    void updateCompany_whenCompanyDoesNotExist_shouldThrowNotFoundException() {
        // ARRANGE
        long companyId = 99L; // An ID that doesn't exist
        CompanyDto requestDto = new CompanyDto(companyId, "Doesn't Matter", "JP", "NEX", null, null, null);
        when(companyRepository.findByIdOptional(companyId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(NotFoundException.class, () -> {
            companyService.updateCompany(companyId, requestDto);
        });
    }
}