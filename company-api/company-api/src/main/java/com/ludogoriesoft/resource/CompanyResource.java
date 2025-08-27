package com.ludogoriesoft.resource;

import com.ludogoriesoft.dto.CompanyDto;
import com.ludogoriesoft.service.CompanyService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * The REST API resource for managing companies.
 * This class defines the public HTTP endpoints.
 */
@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    @Inject
    CompanyService companyService;

    /**
     * Endpoint for creating a new company.
     * Corresponds to: POST /companies
     *
     * @param companyDto The company data from the request body.
     * @return An HTTP 201 Created response with the new company's data.
     */
    @POST
    public Response createCompany(@Valid CompanyDto companyDto) {
        CompanyDto createdCompany = companyService.createCompany(companyDto);
        // Best practice: Return a 201 Created status code along with the newly created resource.
        return Response.status(Response.Status.CREATED).entity(createdCompany).build();
    }

    /**
     * Endpoint for listing all companies.
     * Corresponds to: GET /companies
     *
     * @return An HTTP 200 OK response with a list of all companies.
     */
    @GET
    public List<CompanyDto> getAllCompanies() {
        return companyService.getAllCompanies();
    }
}