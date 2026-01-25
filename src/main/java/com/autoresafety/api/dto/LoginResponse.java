package com.autoresafety.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
