package com.ludogoriesoft.service;

import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.mapper.CompanyMapper;
import com.ludogoriesoft.repository.CompanyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class containing the core business logic for company operations.
 * It coordinates the interaction between the API layer and the repository layer.
 */
@ApplicationScoped
public class CompanyService {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyMapper companyMapper;

    /**
     * Creates a new company in the database.
     * This method is transactional, meaning the entire operation will succeed or fail as a single unit.
     *
     * @param companyDto The DTO containing the data for the new company.
     * @return The DTO of the newly created company, including its generated ID and createdAt timestamp.
     */
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        // 1. Map the incoming DTO to a database entity
        Company company = companyMapper.toEntity(companyDto);

        // 2. Persist the new entity using the repository
        companyRepository.persist(company);

        // 3. Map the persisted entity (which now has an id and createdAt) back to a DTO and return it
        return companyMapper.toDto(company);
    }

    /**
     * Retrieves all companies from the database.
     *
     * @return A list of CompanyDto objects.
     */
    public List<CompanyDto> getAllCompanies() {
        // 1. Fetch all Company entities from the database
        List<Company> companies = companyRepository.listAll();

        // 2. Use a Java Stream to map each entity to a DTO
        return companies.stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }
}