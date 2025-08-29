package com.ludogoriesoft.dto;

import java.time.Instant;

public record CompanyStockDto(
        // Fields from our Company table
        Long id,
        String name,
        String country,
        String symbol,
        String website,
        String email,
        Instant createdAt,

        // Fields from Finnhub (and our StockData table)
        Double marketCapitalization,
        Double shareOutstanding
) {}