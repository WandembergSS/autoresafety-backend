package com.autoresafety.api.dto;

import java.util.List;

import com.autoresafety.api.dto.project.ProjectDocumentDto;

public record StepOneProjectInformationDto(
        String lastUpdatedBy,
        ProjectDocumentDto.Step1ScopeDto.GeneralSummaryDto generalSummary,
        String objectives,
        List<ProjectDocumentDto.Step1ScopeDto.ResourceDto> resources,
        List<ProjectDocumentDto.Step1ScopeDto.SystemComponentDto> systemComponents,
        List<ProjectDocumentDto.Step1ScopeDto.AccidentDto> accidents,
        List<ProjectDocumentDto.Step1ScopeDto.HazardDto> hazards,
        List<ProjectDocumentDto.Step1ScopeDto.SafetyConstraintDto> safetyConstraints,
        List<ProjectDocumentDto.Step1ScopeDto.ResponsibilityDto> responsibilities,
        List<ProjectDocumentDto.Step1ScopeDto.ArtefactDto> artefacts
) {
}
