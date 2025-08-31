package com.ludogoriesoft.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Represents a company entity that will be mapped to the 'companies' table in the database.
 * By extending PanacheEntity, we automatically get an 'id' field of type Long.
 * This entity follows the standard JavaBean pattern with private fields and public accessors.
 */
@Entity
@Table(name = "companies")
public class Company extends PanacheEntity {

    // All fields are now private to enforce encapsulation
    @Column(nullable = false)
    private String name;

    @Column(length = 2, nullable = false)
    private String country;

    @Column(unique = true, nullable = false)
    private String symbol;

    private String website;

    private String email;

    // A mandatory, auto-generated timestamp
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}