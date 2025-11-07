package com.autoresafety.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectPayload(
        @NotBlank @Size(max = 120) String name,
    @Size(max = 1024) String description,
    @Size(max = 40) String status) {

    public ProjectPayload {
        status = (status == null || status.isBlank()) ? "draft" : status;
    }
}
