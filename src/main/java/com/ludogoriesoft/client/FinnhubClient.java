package com.ludogoriesoft.client;

import com.ludogoriesoft.dto.FinnhubProfileDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// This annotation tells Quarkus to treat this interface as a REST Client.
// The configKey points to the configuration we added in application.properties.
@RegisterRestClient(configKey = "com.ludogoriesoft.client.FinnhubClient")
public interface FinnhubClient {

    @GET
    @Path("/stock/profile2")
    FinnhubProfileDto getCompanyProfile(@QueryParam("symbol") String symbol, @QueryParam("token") String apiToken);
}