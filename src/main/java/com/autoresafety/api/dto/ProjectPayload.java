package com.autoresafety.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.autoresafety.domain.ProjectStatus;

public record ProjectPayload(
        @NotBlank @Size(max = 120) String name,
    @Size(max = 1024) String description,
    ProjectStatus status) {

    public ProjectPayload {
        status = (status == null) ? ProjectStatus.PENDING : status;
    }
}
