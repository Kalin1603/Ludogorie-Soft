package com.ludogoriesoft.resource;

import com.ludogoriesoft.client.FinnhubClient;
import com.ludogoriesoft.dto.FinnhubProfileDto;
import com.ludogoriesoft.entity.Company;
import com.ludogoriesoft.repository.CompanyRepository;
import com.ludogoriesoft.repository.StockDataRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class CompanyResourceTest {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    StockDataRepository stockDataRepository;

    // We mock the external client to control its behavior during tests
    @InjectMock
    @RestClient
    FinnhubClient finnhubClient;

    private Long testCompanyId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Cleaning the database before each test to ensure isolation
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();

        // Create a predictable company for our tests to use
        Company company = new Company();
        company.setName("Test Corp");
        company.setCountry("US");
        company.setSymbol("TC");
        companyRepository.persist(company);
        testCompanyId = company.id; // Store its generated ID
    }

    @AfterEach
    @Transactional
    void tearDown() {
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void testGetAllCompaniesEndpoint() {
        given()
                .when().get("/companies")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("Test Corp"));
    }

    @Test
    void testCreateCompanyEndpoint_Success() {
        String newCompanyJson = "{\"name\":\"NewCo\",\"country\":\"DE\",\"symbol\":\"NCO\"}";
        given()
                .contentType(ContentType.JSON).body(newCompanyJson)
                .when().post("/companies")
                .then()
                .statusCode(201)
                .body("name", equalTo("NewCo"));
    }

    @Test
    void testCreateCompanyEndpoint_Conflict() {
        String duplicateCompanyJson = "{\"name\":\"Duplicate\",\"country\":\"FR\",\"symbol\":\"TC\"}";
        given()
                .contentType(ContentType.JSON).body(duplicateCompanyJson)
                .when().post("/companies")
                .then()
                .statusCode(409);
    }

    // THIS TEST COVERS THE UPDATE LOGIC
    @Test
    void testUpdateCompanyEndpoint_Success() {
        String updatedCompanyJson = "{\"name\":\"Updated Corp\",\"country\":\"UK\",\"symbol\":\"TCU\"}";
        given()
                .contentType(ContentType.JSON).body(updatedCompanyJson)
                .when().put("/companies/" + testCompanyId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Corp"))
                .body("country", equalTo("UK"));
    }

    // THIS TEST COVERS THE 'NOT FOUND' CASE FOR UPDATE
    @Test
    void testUpdateCompanyEndpoint_NotFound() {
        String updatedCompanyJson = "{\"name\":\"Updated Corp\",\"country\":\"UK\",\"symbol\":\"TCU\"}";
        given()
                .contentType(ContentType.JSON).body(updatedCompanyJson)
                .when().put("/companies/9999") // Using an ID that doesn't exist
                .then()
                .statusCode(404); // Assert that we get a Not Found error
    }

    // THIS TEST COVERS THE /STOCKS ENDPOINT (CACHE MISS)
    @Test
    void testGetCompanyWithStocksEndpoint_CacheMiss() {
        // ARRANGE: Set up the mock to return data when the Finnhub client is called
        FinnhubProfileDto mockFinnhubResponse = new FinnhubProfileDto(2500.0, 100.0);
        when(finnhubClient.getCompanyProfile(eq("TC"), anyString())).thenReturn(mockFinnhubResponse);

        // ACT & ASSERT
        given()
                .when().get("/companies/" + testCompanyId + "/stocks")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Corp"))
                .body("marketCapitalization", equalTo(2500.0f))
                .body("shareOutstanding", equalTo(100.0f));
    }
}