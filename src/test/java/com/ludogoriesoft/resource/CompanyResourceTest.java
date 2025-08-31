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
import static org.mockito.Mockito.when;

@QuarkusTest
class CompanyResourceTest {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    StockDataRepository stockDataRepository;

    // Using @InjectMock to replace the real FinnhubClient with a mock during tests
    @InjectMock
    @RestClient
    FinnhubClient finnhubClient;

    private Long testCompanyId;

    @BeforeEach
    @Transactional
    void setUp() {
        stockDataRepository.deleteAll();
        companyRepository.deleteAll();

        Company company = new Company();
        company.setName("Test Corp");
        company.setCountry("US");
        company.setSymbol("TC");
        companyRepository.persist(company);
        testCompanyId = company.id; // Store the ID for other tests
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

    // NEW TEST FOR THE PUT ENDPOINT
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

    // NEW TEST FOR THE GET /STOCKS ENDPOINT
    @Test
    void testGetCompanyWithStocksEndpoint() {
        // ARRANGE: Mock the external Finnhub API call
        FinnhubProfileDto mockFinnhubResponse = new FinnhubProfileDto(2500.0, 100.0);
        when(finnhubClient.getCompanyProfile("TC", null)).thenReturn(mockFinnhubResponse);

        // ACT & ASSERT
        given()
                .when().get("/companies/" + testCompanyId + "/stocks")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Corp"))
                .body("symbol", equalTo("TC"))
                .body("marketCapitalization", equalTo(2500.0f))
                .body("shareOutstanding", equalTo(100.0f));
    }
}