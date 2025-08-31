package com.ludogoriesoft.service;

import com.ludogoriesoft.client.FinnhubClient;
import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.dto.CompanyStockDto;
import com.ludogoriesoft.dto.FinnhubProfileDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.entity.StockData;
import com.ludogoriesoft.mapper.CompanyMapper;
import com.ludogoriesoft.repository.CompanyRepository;
import com.ludogoriesoft.repository.StockDataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class containing the core business logic for company operations.
 * It coordinates the interaction between the API layer and the repository layer.
 */
@ApplicationScoped
public class CompanyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final StockDataRepository stockDataRepository;
    private final FinnhubClient finnhubClient;
    private final String finnhubApiKey;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyMapper companyMapper,
                          StockDataRepository stockDataRepository,
                          @RestClient FinnhubClient finnhubClient,
                          @ConfigProperty(name = "finnhub.api.key") String finnhubApiKey) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.stockDataRepository = stockDataRepository;
        this.finnhubClient = finnhubClient;
        this.finnhubApiKey = finnhubApiKey;
    }

    /**
     * Creates a new company in the database.
     * This method is transactional, meaning the entire operation will succeed or fail as a single unit.
     *
     * @param companyDto The DTO containing the data for the new company.
     * @return The DTO of the newly created company, including its generated ID and createdAt timestamp.
     */
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        // Checks if the symbol already exists
        companyRepository.findBySymbol(companyDto.symbol()).ifPresent(company -> {
            // If exists, throw 409 Conflict
            throw new WebApplicationException("Company with symbol " + companyDto.symbol() + " already exists.", Response.Status.CONFLICT);
        });

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

    /**
     * Updates an existing company in the database.
     * This method is transactional.
     *
     * @param id The ID of the company to update.
     * @param companyDto The DTO with the updated data.
     * @return The DTO of the updated company.
     * @throws NotFoundException if no company with the given ID is found.
     */
    @Transactional
    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        // 1. Find the existing company by its ID.
        Company companyToUpdate = companyRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Company with id " + id + " not found"));

        // 2. Use our new mapper method to update the entity's fields.
        companyMapper.updateEntityFromDto(companyDto, companyToUpdate);

        // 3. Persist the changes. While not always strictly necessary for managed entities
        // within a transaction, it's an explicit and safe way to ensure the update happens.
        companyRepository.persist(companyToUpdate);

        // 4. Map the updated entity back to a DTO and return it.
        return companyMapper.toDto(companyToUpdate);
    }

    /**
     * Gets combined company and stock data. Implements a daily caching mechanism.
     */
    @Transactional
    public CompanyStockDto getCompanyStockData(Long companyId) {
        // 1. First, find the company in our database.
        Company company = companyRepository.findByIdOptional(companyId)
                .orElseThrow(() -> new NotFoundException("Company with id " + companyId + " not found"));

        // 2. Check the cache: Do we already have stock data for this company from today?
        Optional<StockData> cachedStockData = stockDataRepository.findLatestByCompanyIdForToday(companyId);

        StockData stockDataToUse;
        if (cachedStockData.isPresent()) {
            // 3a. CACHE HIT: We found data from today. Use it.
            LOGGER.info("CACHE HIT for company ID: {}", companyId);
            stockDataToUse = cachedStockData.get();
        } else {
            // 3b. CACHE MISS: No data for today. Call the external Finnhub API.
            LOGGER.info("CACHE MISS for company ID: {}. Calling Finnhub API.", companyId);
            FinnhubProfileDto finnhubData = finnhubClient.getCompanyProfile(company.getSymbol(), finnhubApiKey);

            // 4. Create a NEW StockData entity to store the results. We never update old ones.
            StockData newStockData = new StockData();
            newStockData.company = company;
            newStockData.setMarketCapitalization(finnhubData.marketCapitalization());
            newStockData.setShareOutstanding(finnhubData.shareOutstanding());

            // 5. Save the new data to our database for future requests today.
            stockDataRepository.persist(newStockData);
            stockDataToUse = newStockData;
        }

        // 6. Map the company data and the chosen stock data to our final DTO and return it.
        return companyMapper.toCompanyStockDto(company, stockDataToUse);
    }
}