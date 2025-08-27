package com.ludogoriesoft.repository;

import com.ludogoriesoft.entity.Company;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The Repository for the Company entity. Implements the PanacheRepository interface,
 * which provides a full set of default methods for CRUD operations.
 *
 * This class acts as a single source of truth for all database interactions
 * involving the Company entity.
 */
@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {

    // By implementing PanacheRepository<Company>, this class automatically
    // inherits methods like:
    // - persist(Company company)
    // - findById(Long id)
    // - listAll()
    // - delete(Company company)
    // - count()
    // and many more.

    // If we needed a custom query in the future, we would add it here. For example:
    // public Company findBySymbol(String symbol) {
    //     return find("symbol", symbol).firstResult();
    // }
}