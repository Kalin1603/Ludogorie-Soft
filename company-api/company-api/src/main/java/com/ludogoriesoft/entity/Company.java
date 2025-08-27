package com.ludogoriesoft.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Represents a company entity that will be mapped to the 'companies' table in the database.
 * By extending PanacheEntity, we automatically get an 'id' field of type Long,
 * along with many useful, simplified data access methods.
 */
@Entity
@Table(name = "companies")
public class Company extends PanacheEntity {

    // Mandatory properties as per the requirements

    @Column(nullable = false)
    public String name;

    @Column(length = 2, nullable = false)
    public String country;

    @Column(unique = true, nullable = false)
    public String symbol;

    // Optional properties

    public String website;

    public String email;

    // A mandatory, auto-generated timestamp

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant createdAt;
}