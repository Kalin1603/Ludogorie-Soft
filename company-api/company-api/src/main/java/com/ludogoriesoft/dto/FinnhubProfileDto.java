package com.ludogoriesoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// This tells Jackson to ignore any fields in the JSON from Finnhub
// that we haven't defined in our record. This is very important for stability.
@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubProfileDto(
        Double marketCapitalization,
        Double shareOutstanding
) {}