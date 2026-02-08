package com.autoresafety.seed;

import java.time.Instant;
import java.util.List;

import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.api.dto.project.SourceType;
import com.autoresafety.domain.Project;
import com.autoresafety.domain.ProjectStatus;
import com.autoresafety.persistence.ProjectRepository;
import com.autoresafety.service.ProjectDocumentService;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@IfBuildProfile("dev")
public class ProjectDocumentDevSeed {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectDocumentService projectDocumentService;

    @Transactional
    void seed(@Observes StartupEvent event) {
        Project project = projectRepository.findById(2L);
        if (project == null) {
            project = new Project();
            project.id = 2L;
            project.name = "Example Project";
            project.description = "Step 1 sample";
            project.status = ProjectStatus.PENDING;
            projectRepository.persist(project);
        }

        ProjectDocumentDto.ProjectDto projectInfo = ProjectDocumentDto.ProjectDto.builder()
            .id(project.id)
            .name(project.name)
            .domain("Aerospace")
            .owner("AutoRESafety")
            .description(project.description)
            .status(project.status)
            .currentStep(1)
            .createdAt(Instant.parse("2026-02-08T12:00:00Z"))
            .updatedAt(Instant.parse("2026-02-08T12:00:00Z"))
            .build();

        ProjectDocumentDto.Step1ScopeDto.GeneralSummaryDto summary = ProjectDocumentDto.Step1ScopeDto.GeneralSummaryDto.builder()
            .assumptions("Assume nominal operating conditions")
            .systemDefinition("Autonomous inspection drone system")
            .systemBoundary("Drone + control station + cloud telemetry")
            .outOfScope("Manufacturing process and supply chain")
            .build();

        ProjectDocumentDto.Step1ScopeDto step1 = ProjectDocumentDto.Step1ScopeDto.builder()
            .lastUpdatedBy("admin")
            .generalSummary(summary)
            .objectives("Prevent collision")
            .resources(List.of(
            ProjectDocumentDto.Step1ScopeDto.ResourceDto.builder()
                .id(1L)
                .name("Flight manual")
                .category("documentation")
                .reference("DOC-001")
                .sourceType(SourceType.MANUAL)
                .build()
            ))
            .systemComponents(List.of(
            ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                .id(1L)
                .name("Onboard controller")
                .description("Executes navigation logic")
                .build()
            ))
            .accidents(List.of(
            ProjectDocumentDto.Step1ScopeDto.AccidentDto.builder()
                .id(1L)
                .code("A-1")
                .description("Drone collision with obstacle")
                .build()
            ))
            .hazards(List.of(
            ProjectDocumentDto.Step1ScopeDto.HazardDto.builder()
                .id(1L)
                .code("H-1")
                .description("Loss of obstacle detection")
                .linkedAccidents(List.of("A-1"))
                .build()
            ))
            .safetyConstraints(List.of(
            ProjectDocumentDto.Step1ScopeDto.SafetyConstraintDto.builder()
                .id(1L)
                .code("SC-1")
                .statement("The drone shall maintain safe separation from obstacles")
                .linkedHazards(List.of("H-1"))
                .build()
            ))
            .responsibilities(List.of(
            ProjectDocumentDto.Step1ScopeDto.ResponsibilityDto.builder()
                .id(1L)
                .component("Navigation module")
                .responsibility("Maintain separation logic")
                .linkedConstraints(List.of("SC-1"))
                .build()
            ))
            .artefacts(List.of(
            ProjectDocumentDto.Step1ScopeDto.ArtefactDto.builder()
                .id(1L)
                .name("System architecture")
                .purpose("Describe components")
                .reference("ARCH-001")
                .build()
            ))
            .build();

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(projectInfo)
            .step1Scope(step1)
            .build();

        projectDocumentService.saveOrUpdate(project.id, document);
    }
}
