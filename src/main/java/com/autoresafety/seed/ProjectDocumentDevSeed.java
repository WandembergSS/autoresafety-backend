package com.autoresafety.seed;

import java.time.Instant;
import java.time.LocalDate;
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
        Project project = projectRepository.findById(1L);
        if (project == null) {
            project = new Project();
            project.id = 1L;
            project.name = "Chapter Case Study Project";
            project.description = "Complete STPA chapter case for application";
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
            .currentStep(7)
            .createdAt(Instant.parse("2026-02-08T12:00:00Z"))
            .updatedAt(Instant.parse("2026-06-09T10:00:00Z"))
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
                .name("Autopilot")
                .description("Executes autonomous navigation logic")
                .build(),
            ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                .id(2L)
                .name("Operator")
                .description("Human operator issuing supervisory commands")
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

        ProjectDocumentDto.Step2IstarDto step2 = ProjectDocumentDto.Step2IstarDto.builder()
            .actors(List.of(
                ProjectDocumentDto.Step2IstarDto.ActorDto.builder()
                    .id(1L)
                    .name("Operator")
                    .type("human")
                    .responsibilities(List.of("Start mission", "Monitor telemetry"))
                    .build(),
                ProjectDocumentDto.Step2IstarDto.ActorDto.builder()
                    .id(2L)
                    .name("Autopilot")
                    .type("software")
                    .responsibilities(List.of("Path planning", "Obstacle avoidance"))
                    .build()
            ))
            .goalLinks(List.of(
                ProjectDocumentDto.Step2IstarDto.GoalLinkDto.builder()
                    .id(1L)
                    .fromActor("Operator")
                    .goal("Maintain safe mission execution")
                    .linkType("delegates")
                    .build(),
                ProjectDocumentDto.Step2IstarDto.GoalLinkDto.builder()
                    .id(2L)
                    .fromActor("Autopilot")
                    .goal("Avoid obstacles in real time")
                    .linkType("achieves")
                    .build()
            ))
            .build();

        ProjectDocumentDto.Step3ControlStructureDto step3 = ProjectDocumentDto.Step3ControlStructureDto.builder()
            .controlActions(List.of(
                ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .action("Adjust heading")
                    .controlledProcess("")
                    .feedback("IMU + lidar")
                    .build(),
                ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                    .id(2L)
                    .controller("Autopilot")
                    .action("Pause mission")
                    .controlledProcess("Operator")
                    .feedback("Ground station UI")
                    .build()
            ))
            .feedbackLoops(List.of(
                ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto.builder()
                    .id(1L)
                    .source("Lidar")
                    .destination("Autopilot")
                    .signal("Distance to obstacle")
                    .latency("<100ms")
                    .build()
            ))
            .build();

        ProjectDocumentDto.Step4UcasDto step4 = ProjectDocumentDto.Step4UcasDto.builder()
            .ucas(List.of(
                ProjectDocumentDto.Step4UcasDto.UcaDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .controlAction("Adjust heading")
                    .hazard("H-1")
                    .category("Not provided")
                    .build(),
                ProjectDocumentDto.Step4UcasDto.UcaDto.builder()
                    .id(2L)
                    .controller("Operator")
                    .controlAction("Pause mission")
                    .hazard("H-1")
                    .category("Provided too late")
                    .build()
            ))
            .build();

        ProjectDocumentDto.Step5ControllerConstraintsDto step5 = ProjectDocumentDto.Step5ControllerConstraintsDto.builder()
            .constraints(List.of(
                ProjectDocumentDto.Step5ControllerConstraintsDto.ConstraintDto.builder()
                    .id(1L)
                    .ucaRef("UCA-1")
                    .constraint("Autopilot shall command evasive heading when obstacle distance is below threshold")
                    .enforcementMechanism("Runtime rule in navigation loop")
                    .status("defined")
                    .build()
            ))
            .build();

        ProjectDocumentDto.Step6LossScenariosDto step6 = ProjectDocumentDto.Step6LossScenariosDto.builder()
            .lossScenarios(List.of(
                ProjectDocumentDto.Step6LossScenariosDto.LossScenarioDto.builder()
                    .id(1L)
                    .uca("UCA-1")
                    .hazard("H-1")
                    .outcome("Drone impacts static obstacle")
                    .severity("High")
                    .mitigations(List.of("Add sensor health check", "Fallback to hover"))
                    .status("open")
                    .build()
            ))
            .safetyRequirements(List.of(
                ProjectDocumentDto.Step6LossScenariosDto.SafetyRequirementDto.builder()
                    .id(1L)
                    .title("SR-1 Maintain safe obstacle clearance")
                    .linkedScenario(1L)
                    .category("functional")
                    .owner("Safety team")
                    .dueDate(LocalDate.of(2026, 7, 15))
                    .status("planned")
                    .build()
            ))
            .build();

        ProjectDocumentDto.Step7ModelUpdateDto step7 = ProjectDocumentDto.Step7ModelUpdateDto.builder()
            .modelChanges(List.of(
                ProjectDocumentDto.Step7ModelUpdateDto.ModelChangeDto.builder()
                    .id(1L)
                    .area("Navigation")
                    .change("Added obstacle-clearance guard before heading update")
                    .driver("UCA-1")
                    .impact("Reduces collision likelihood")
                    .status("implemented")
                    .evidence(List.of("SIM-REP-101", "TEST-LOG-224"))
                    .build()
            ))
            .validationTasks(List.of(
                ProjectDocumentDto.Step7ModelUpdateDto.ValidationTaskDto.builder()
                    .id(1L)
                    .name("Hardware-in-loop obstacle test")
                    .owner("QA")
                    .dueDate("2026-07-20")
                    .channel("test-lab")
                    .status("queued")
                    .build()
            ))
            .integrationNotes(List.of(
                ProjectDocumentDto.Step7ModelUpdateDto.IntegrationNoteDto.builder()
                    .id(1L)
                    .summary("Navigation and telemetry schemas aligned")
                    .createdOn("2026-06-09")
                    .author("System Architect")
                    .actionItems(List.of("Run regression pack", "Update ops playbook"))
                    .build()
            ))
            .build();

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(projectInfo)
            .step1Scope(step1)
            .step2Istar(step2)
            .step3ControlStructure(step3)
            .step4Ucas(step4)
            .step5ControllerConstraints(step5)
            .step6LossScenarios(step6)
            .step7ModelUpdate(step7)
            .build();

        projectDocumentService.saveOrUpdate(project.id, document);
    }
}
