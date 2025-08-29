package com.ludogoriesoft.repository;

import com.ludogoriesoft.entity.Company;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * The Repository for the Company entity. Implements the PanacheRepository interface,
 * which provides a full set of default methods for CRUD operations.
 *
 * This class acts as a single source of truth for all database interactions
 * involving the Company entity.
 */
@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {

    public Optional<Company> findBySymbol(String symbol) {
        return find("symbol", symbol).firstResultOptional();
    }
}