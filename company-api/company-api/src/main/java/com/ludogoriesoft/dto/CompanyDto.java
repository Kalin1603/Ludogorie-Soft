package com.ludogoriesoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * A Data Transfer Object for the Company entity.
 * This record defines the public API contract for a company.
 * It includes validation annotations to ensure incoming data is well-formed.
 */
public record CompanyDto(
        Long id,

        @NotBlank(message = "Company name cannot be blank")
        String name,

        @NotBlank(message = "Country code is mandatory")
        @Size(min = 2, max = 2, message = "Country code must be 2 characters")
        String country,

        @NotBlank(message = "Symbol is mandatory")
        String symbol,

        String website,

        // This ensures that if an email is provided, it must be in a valid format.
        @Email(message = "Please provide a valid email address")
        String email,

        Instant createdAt
) {}