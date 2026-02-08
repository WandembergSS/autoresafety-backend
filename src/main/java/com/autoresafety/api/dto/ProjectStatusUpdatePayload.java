package com.autoresafety.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProjectStatusUpdatePayload(
        @NotNull @Positive Long id,
        @NotBlank String status
) {
}
