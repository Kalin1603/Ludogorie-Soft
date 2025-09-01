package com.ludogoriesoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// This tells Jackson to ignore any fields in the JSON from Finnhub
// that we haven't defined in our record. This is very important for stability.
@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubProfileDto(
        Double marketCapitalization,
        Double shareOutstanding,

        // New, static fields for better testing and data quality
        String name,
        String country,
        @JsonProperty("ticker")
        String symbol
) {}