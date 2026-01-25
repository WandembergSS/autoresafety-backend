package com.autoresafety.api;

import java.util.HashMap;
import java.util.Map;
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

    private static final String UNSET = "__UNSET__";

    /**
     * Preferred: comma-separated username:password pairs, e.g. "admin:secret,alice:pass".
     * Backwards-compatible fallback: auth.username/auth.password.
     */
    @ConfigProperty(name = "auth.users", defaultValue = UNSET)
    String configuredUsers;

    @ConfigProperty(name = "auth.username", defaultValue = UNSET)
    String configuredUsername;

    @ConfigProperty(name = "auth.password", defaultValue = UNSET)
    String configuredPassword;

    @ConfigProperty(name = "auth.jwt.lifespan-seconds")
    long lifespanSeconds;

    @POST
    @Path("/login")
    @Transactional
    public Response login(@Valid LoginRequest request) {
        if (!isValidCredential(request.username(), request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid credentials"))
                    .build();
        }

        String token = Jwt.upn(request.username())
                .groups(Set.of("User"))
                .sign();

        return Response.ok(new LoginResponse(token, "Bearer", lifespanSeconds)).build();
    }

    private boolean isValidCredential(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        Map<String, String> users = parseUsers(configuredUsers);
        if (!users.isEmpty()) {
            return password.equals(users.get(username));
        }

        // fallback (legacy single-user config)
        if (UNSET.equals(configuredUsername) || UNSET.equals(configuredPassword)) {
            return false;
        }
        return configuredUsername.equals(username) && configuredPassword.equals(password);
    }

    private static Map<String, String> parseUsers(String configuredUsers) {
        Map<String, String> users = new HashMap<>();
        if (configuredUsers == null) {
            return users;
        }

        if (UNSET.equals(configuredUsers)) {
            return users;
        }

        String trimmedAll = configuredUsers.trim();
        if (trimmedAll.isEmpty()) {
            return users;
        }

        for (String entry : trimmedAll.split(",")) {
            if (entry == null) {
                continue;
            }
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colon = trimmed.indexOf(':');
            if (colon <= 0 || colon == trimmed.length() - 1) {
                continue;
            }
            String u = trimmed.substring(0, colon).trim();
            String p = trimmed.substring(colon + 1).trim();
            if (!u.isEmpty() && !p.isEmpty()) {
                users.put(u, p);
            }
        }

        return users;
    }

    public record ErrorResponse(String message) {
    }
}
