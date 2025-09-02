package com.ludogoriesoft.client;

import com.ludogoriesoft.dto.FinnhubProfileDto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("Finnhub Client Integration Tests")
class FinnhubClientIT {

    @Inject
    @RestClient
    FinnhubClient finnhubClient;

    // This property is INJECTED from .env file and MUST be a VALID key.
    @ConfigProperty(name = "finnhub.api.key")
    String validApiKeyFromEnv;

     // This test uses the REAL key from the .env file.
     @Test
     @DisplayName("Should return valid and correct company data when using a valid API key")
     @EnabledIfSystemProperty(named = "RUN_INTEGRATION_TESTS", matches = "true")
     void getCompanyProfile_withValidKey_returnsValidData() {
         // Arrange
         String symbol = "AAPL";
         assertFalse(validApiKeyFromEnv.isBlank() || validApiKeyFromEnv.equals("UNSET"),
                 "The FINNHUB_API_KEY in your .env file is not set correctly.");

         // Act
         FinnhubProfileDto profile = finnhubClient.getCompanyProfile(symbol, validApiKeyFromEnv);

         // Assert
         assertNotNull(profile, "The profile DTO should not be null for a valid response.");

         // 1. Plausibility Check (for dynamic data)
         assertNotNull(profile.marketCapitalization(), "Market capitalization should not be null.");
         assertTrue(profile.marketCapitalization() > 0, "Market capitalization must be a positive value.");

         // 2. Data Integrity Check (for static data using our enhanced DTO)
         assertEquals("Apple Inc", profile.name(), "The company name should be correct for the symbol.");
         assertEquals("US", profile.country(), "The country should be correct for the symbol.");
         assertEquals("AAPL", profile.symbol(), "The ticker symbol in the response should match the request.");
     }

     // Test 2: Verifies the API's "failure contract" for authentication.

    @Test
    @DisplayName("Should throw WebApplicationException when using an invalid API key")
    @EnabledIfSystemProperty(named = "RUN_INTEGRATION_TESTS", matches = "true")
    void getCompanyProfile_withInvalidKey_throwsException() {
        // Arrange
        String symbol = "AAPL";
        // This key is fake and is defined only for this test.
        String intentionallyInvalidApiKey = "this-key-is-deliberately-invalid-for-testing";

        // Act & Assert
        // Expecting the client call to fail because the Finnhub API will return an HTTP 401 Unauthorized status.
        WebApplicationException thrown = assertThrows(WebApplicationException.class,
                () -> finnhubClient.getCompanyProfile(symbol, intentionallyInvalidApiKey),
                "A WebApplicationException should be thrown for an invalid API key.");

        assertEquals(401, thrown.getResponse().getStatus(), "The HTTP status code should be 401 Unauthorized.");
    }
}