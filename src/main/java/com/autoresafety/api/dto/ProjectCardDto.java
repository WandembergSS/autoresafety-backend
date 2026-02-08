package com.autoresafety.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autoresafety.domain.ProjectStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCardDto {
    private Long id;
    private String name;
    private String domain;
    private String owner;
    private String description;
    private ProjectStatus status;
    private Integer currentStep;
}
