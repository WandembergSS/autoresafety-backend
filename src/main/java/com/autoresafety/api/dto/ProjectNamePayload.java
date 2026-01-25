package com.autoresafety.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectNamePayload {
    @NotBlank
    private String name;

    private String description;
    private String domain;
    private String owner;
    private Integer currentStep;
}
