package com.autoresafety.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/api/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AiResource {

    @Inject
    @RestClient
    AiServiceClient aiServiceClient;

    @POST
    @Path("/ask")
    public JsonNode ask(JsonNode payload) {
        return aiServiceClient.ask(payload);
    }
}
