package com.autoresafety.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.autoresafety.api.dto.ProjectNamePayload;
import com.autoresafety.api.dto.ProjectPayload;
import com.autoresafety.api.dto.ProjectResumeDto;
import com.autoresafety.api.dto.ProjectStatusUpdatePayload;
import com.autoresafety.api.dto.StepOneProjectInformationDto;
import com.autoresafety.api.dto.StepOneProjectUpdatePayload;
import com.autoresafety.api.dto.StepThreeProjectUpdatePayload;
import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.domain.Project;
import com.autoresafety.domain.ProjectDocument;
import com.autoresafety.domain.ProjectStatus;
import com.autoresafety.persistence.ProjectRepository;
import com.autoresafety.service.ProjectDocumentService;

import io.quarkus.panache.common.Sort;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class ProjectResource {

    private static final Set<String> STEP3_OPTIONAL_TYPES = Set.of(
        "feedback",
        "process model",
        "control algorithm",
        "actuator",
        "sensor",
        "external input"
    );

    @Inject
    ProjectRepository repository;

    @Inject
    ProjectDocumentService projectDocumentService;

    @GET
    public List<Project> list() {
        return repository.listAll(Sort.by("name"));
    }

    @GET
    @Path("/{id}")
    public Project getById(@PathParam("id") Long id) {
        return repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{id}/full")
    public ProjectDocumentDto getFullInformation(@PathParam("id") Long id) {
        Project project = repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);

        ProjectDocumentDto.ProjectDto projectInfo = document == null || document.getProject() == null
            ? ProjectDocumentDto.ProjectDto.builder().build()
            : document.getProject();

        if (projectInfo.getId() == null) {
            projectInfo.setId(project.id);
        }
        if (projectInfo.getName() == null) {
            projectInfo.setName(project.name);
        }
        if (projectInfo.getDescription() == null) {
            projectInfo.setDescription(project.description);
        }
        if (projectInfo.getStatus() == null) {
            projectInfo.setStatus(project.status);
        }
        if (projectInfo.getCreatedAt() == null && project.createdAt != null) {
            projectInfo.setCreatedAt(project.createdAt.toInstant());
        }
        if (projectInfo.getUpdatedAt() == null && project.updatedAt != null) {
            projectInfo.setUpdatedAt(project.updatedAt.toInstant());
        }

        if (document == null) {
            return ProjectDocumentDto.builder()
                .project(projectInfo)
                .build();
        }

        document.setProject(projectInfo);
        return document;
    }

    @GET
    @Path("/step_one_project_information/{id}")
    public StepOneProjectInformationDto getStepOneInformation(@PathParam("id") Long id) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        if (document == null) {
            throw new NotFoundException();
        }
        if (document.getStep1Scope() == null) {
            return new StepOneProjectInformationDto(null, null, null, null, null, null, null, null, null, null);
        }

        ProjectDocumentDto.Step1ScopeDto step1 = document.getStep1Scope();
        return new StepOneProjectInformationDto(
            step1.getLastUpdatedBy(),
            step1.getGeneralSummary(),
            formatObjectives(step1.getObjectives()),
            step1.getResources(),
            step1.getSystemComponents(),
            step1.getAccidents(),
            step1.getHazards(),
            step1.getSafetyConstraints(),
            step1.getResponsibilities(),
            step1.getArtefacts()
        );
    }

    @GET
    @Path("/step_two_project_information/{id}")
    public ProjectDocumentDto.Step2IstarDto getStepTwoInformation(@PathParam("id") Long id) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        if (document == null) {
            throw new NotFoundException();
        }

        if (document.getStep2Istar() == null) {
            return ProjectDocumentDto.Step2IstarDto.builder()
                .actors(null)
                .goalLinks(null)
                .build();
        }

        return document.getStep2Istar();
    }

    @GET
    @Path("/step_three_project_information/{id}")
    public ProjectDocumentDto.Step3ControlStructureDto getStepThreeInformation(@PathParam("id") Long id) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        if (document == null) {
            throw new NotFoundException();
        }

        if (document.getStep3ControlStructure() == null) {
            return ProjectDocumentDto.Step3ControlStructureDto.builder()
                .controlActions(null)
                .feedbackLoops(null)
                .optionalElements(null)
                .build();
        }

        return document.getStep3ControlStructure();
    }

    @POST
    @Path("/step_one_project_update")
    @Transactional
    public Response updateStepOne(@Valid StepOneProjectUpdatePayload payload) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(payload.id());
        if (document == null) {
            throw new NotFoundException();
        }

        ProjectDocumentDto.Step1ScopeDto step1 = document.getStep1Scope();
        if (step1 == null) {
            step1 = ProjectDocumentDto.Step1ScopeDto.builder().build();
        }

        if (payload.lastUpdatedBy() != null) {
            step1.setLastUpdatedBy(payload.lastUpdatedBy());
        }
        if (payload.generalSummary() != null) {
            step1.setGeneralSummary(payload.generalSummary());
        }
        if (payload.objectives() != null) {
            step1.setObjectives(payload.objectives());
        }
        if (payload.resources() != null) {
            step1.setResources(payload.resources());
        }
        if (payload.systemComponents() != null) {
            step1.setSystemComponents(payload.systemComponents());
        }
        if (payload.accidents() != null) {
            step1.setAccidents(payload.accidents());
        }
        if (payload.hazards() != null) {
            step1.setHazards(payload.hazards());
        }
        if (payload.safetyConstraints() != null) {
            step1.setSafetyConstraints(payload.safetyConstraints());
        }
        if (payload.responsibilities() != null) {
            step1.setResponsibilities(payload.responsibilities());
        }
        if (payload.artefacts() != null) {
            step1.setArtefacts(payload.artefacts());
        }

        document.setStep1Scope(step1);
        projectDocumentService.saveOrUpdate(payload.id(), document);

        return Response.ok().build();
    }

    @POST
    @Path("/step_three_project_update")
    @Transactional
    public Response updateStepThree(@Valid StepThreeProjectUpdatePayload payload) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(payload.id());
        if (document == null) {
            throw new NotFoundException();
        }

        ProjectDocumentDto.Step3ControlStructureDto current = document.getStep3ControlStructure();
        if (current == null) {
            current = ProjectDocumentDto.Step3ControlStructureDto.builder().build();
        }

        if (payload.step3Information() != null) {
            Map<String, String> entityNamesById = new HashMap<>();
            if (payload.step3Information().entities() != null) {
                for (StepThreeProjectUpdatePayload.EntityPayload entity : payload.step3Information().entities()) {
                    if (entity != null && entity.id() != null && entity.name() != null && !entity.name().isBlank()) {
                        entityNamesById.put(entity.id(), entity.name().trim());
                    }
                }
            }

            if (payload.step3Information().controlActions() != null) {
                List<ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto> mappedActions = new ArrayList<>();
                for (StepThreeProjectUpdatePayload.ControlActionPayload action : payload.step3Information().controlActions()) {
                    if (action == null) {
                        continue;
                    }

                    mappedActions.add(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                        .id(parseNumericId(action.id()))
                        .controller(resolveEntityName(entityNamesById, action.sourceEntityId()))
                        .action(action.action())
                        .controlledProcess(resolveEntityName(entityNamesById, action.targetEntityId()))
                        .feedback(null)
                        .build());
                }
                current.setControlActions(mappedActions);
            }

            if (payload.step3Information().optionalElements() != null) {
                List<ProjectDocumentDto.Step3ControlStructureDto.OptionalElementDto> mappedOptionalElements = new ArrayList<>();
                List<ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto> feedbackLoops = new ArrayList<>();
                for (StepThreeProjectUpdatePayload.OptionalElementPayload element : payload.step3Information().optionalElements()) {
                    if (element == null) {
                        continue;
                    }

                    String normalizedType = normalizeOptionalType(element.type());
                    String normalizedSourceKind = normalizeKind(element.sourceKind(), "sourceKind", element.id());
                    String normalizedDestinationKind = normalizeKind(element.destinationKind(), "destinationKind", element.id());

                    mappedOptionalElements.add(ProjectDocumentDto.Step3ControlStructureDto.OptionalElementDto.builder()
                        .id(element.id())
                        .type(normalizedType)
                        .name(element.name())
                        .sourceKind(normalizedSourceKind)
                        .sourceEntityId(element.sourceEntityId())
                        .sourceExternalId(element.sourceExternalId())
                        .destinationKind(normalizedDestinationKind)
                        .destinationEntityId(element.destinationEntityId())
                        .destinationExternalId(element.destinationExternalId())
                        .responsibilityId(element.responsibilityId())
                        .build());

                    if (!"Feedback".equals(normalizedType)) {
                        continue;
                    }

                    feedbackLoops.add(ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto.builder()
                        .id(parseNumericId(element.id()))
                        .source(resolveEndpointName(entityNamesById, element.sourceEntityId(), element.sourceExternalId()))
                        .destination(resolveEndpointName(entityNamesById, element.destinationEntityId(), element.destinationExternalId()))
                        .signal(element.name())
                        .latency(null)
                        .build());
                }
                current.setOptionalElements(mappedOptionalElements);
                current.setFeedbackLoops(feedbackLoops);
            }
        }

        document.setStep3ControlStructure(current);
        projectDocumentService.saveOrUpdate(payload.id(), document);
        return Response.ok().build();
    }

    private static String formatObjectives(String objectives) {
        if (objectives == null || objectives.isBlank()) {
            return null;
        }
        return objectives.trim();
    }

    private static String resolveEntityName(Map<String, String> entityNamesById, String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return null;
        }
        return entityNamesById.getOrDefault(entityId, entityId);
    }

    private static String resolveEndpointName(Map<String, String> entityNamesById, String entityId, String externalId) {
        String entityName = resolveEntityName(entityNamesById, entityId);
        if (entityName != null) {
            return entityName;
        }
        if (externalId == null || externalId.isBlank()) {
            return null;
        }
        return externalId;
    }

    private static String normalizeOptionalType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw stepThreeValidationError("optionalElements.type is required");
        }

        String normalized = rawType.trim().toLowerCase(Locale.ROOT);
        if (!STEP3_OPTIONAL_TYPES.contains(normalized)) {
            throw stepThreeValidationError("Invalid optionalElements.type: " + rawType);
        }

        return switch (normalized) {
            case "feedback" -> "Feedback";
            case "process model" -> "Process Model";
            case "control algorithm" -> "Control Algorithm";
            case "actuator" -> "Actuator";
            case "sensor" -> "Sensor";
            case "external input" -> "External Input";
            default -> throw stepThreeValidationError("Invalid optionalElements.type: " + rawType);
        };
    }

    private static String normalizeKind(String rawKind, String field, String optionalId) {
        if (rawKind == null || rawKind.isBlank()) {
            throw stepThreeValidationError(field + " is required for optional element " + optionalId);
        }

        String normalized = rawKind.trim().toLowerCase(Locale.ROOT);
        if (!"entity".equals(normalized) && !"external".equals(normalized)) {
            throw stepThreeValidationError("Invalid " + field + ": " + rawKind);
        }
        return normalized;
    }

    private static WebApplicationException stepThreeValidationError(String message) {
        return new WebApplicationException(
            Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", message))
                .build()
        );
    }

    private static Long parseNumericId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(rawId.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @POST
    @Transactional
    public Response create(@Valid ProjectPayload payload) {
        Project project = new Project();
        project.name = payload.name();
        project.description = payload.description();
        project.status = payload.status();
        repository.persist(project);
        return Response.created(URI.create("/api/projects/" + project.id)).entity(project).build();
    }

    @POST
    @Path("/minimal-project-creation")
    @Transactional
    public Response createMinimal(@Valid ProjectNamePayload payload) {
        Project project = new Project();
        project.name = payload.getName();
        project.description = payload.getDescription();
        repository.persist(project);

        ProjectDocumentDto.ProjectDto projectInfo = ProjectDocumentDto.ProjectDto.builder()
            .id(project.id)
            .name(project.name)
            .description(project.description)
            .status(project.status)
            .domain(payload.getDomain())
            .owner(payload.getOwner())
            .currentStep(payload.getCurrentStep())
            .build();

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(projectInfo)
            .build();

        projectDocumentService.saveOrUpdate(project.id, document);

        ProjectResumeDto response = ProjectResumeDto.builder()
            .id(project.id)
            .name(project.name)
            .description(project.description)
            .status(project.status)
            .domain(payload.getDomain())
            .owner(payload.getOwner())
            .currentStep(payload.getCurrentStep())
            .build();

        return Response.created(URI.create("/api/projects/" + project.id)).entity(response).build();
    }

    @POST
    @Path("/minimal-project-update")
    @PermitAll
    @Transactional
    public Response updateMinimalStatus(@Valid ProjectStatusUpdatePayload payload) {
        Project project = repository.findByIdOptional(payload.id()).orElseThrow(NotFoundException::new);
        String requestedStatus = payload.status();
        if (requestedStatus != null && requestedStatus.equalsIgnoreCase("REMOVED")) {
            ProjectDocument.delete("project.id", project.id);
            boolean deleted = repository.deleteById(project.id);
            if (!deleted) {
                throw new NotFoundException();
            }
            return Response.noContent().build();
        }

        ProjectStatus status = parseStatus(requestedStatus);
        project.status = status;

        ProjectDocumentDto document = projectDocumentService.getByProjectId(project.id);
        ProjectDocumentDto.ProjectDto projectInfo = document == null ? null : document.getProject();
        if (projectInfo != null) {
            projectInfo.setStatus(project.status);
            projectDocumentService.saveOrUpdate(project.id, document);
        }

        ProjectResumeDto response = ProjectResumeDto.builder()
            .id(project.id)
            .name(project.name)
            .description(project.description)
            .status(project.status)
            .domain(projectInfo == null ? null : projectInfo.getDomain())
            .owner(projectInfo == null ? null : projectInfo.getOwner())
            .currentStep(projectInfo == null ? null : projectInfo.getCurrentStep())
            .build();

        return Response.ok(response).build();
    }

    private static ProjectStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new BadRequestException("status is required");
        }

        String normalized = rawStatus.trim().toUpperCase();
        if ("CANCELED".equals(normalized)) {
            normalized = "CANCELLED";
        }

        try {
            return ProjectStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status: " + rawStatus);
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Project update(@PathParam("id") Long id, @Valid ProjectPayload payload) {
        Project project = repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
        project.name = payload.name();
        project.description = payload.description();
        project.status = payload.status();
        return project;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }
}
