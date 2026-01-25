package com.autoresafety.api;

import java.util.Set;

import com.autoresafety.api.dto.LoginRequest;
import com.autoresafety.api.dto.LoginResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.jwt.build.Jwt;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @ConfigProperty(name = "auth.username")
    String configuredUsername;

    @ConfigProperty(name = "auth.password")
    String configuredPassword;

    @ConfigProperty(name = "auth.jwt.lifespan-seconds")
    long lifespanSeconds;

    @POST
    @Path("/login")
    @Transactional
    public Response login(@Valid LoginRequest request) {
        if (!configuredUsername.equals(request.username()) || !configuredPassword.equals(request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid credentials"))
                    .build();
        }

        String token = Jwt.upn(request.username())
                .groups(Set.of("User"))
                .sign();

        return Response.ok(new LoginResponse(token, "Bearer", lifespanSeconds)).build();
    }

    public record ErrorResponse(String message) {
    }
}
