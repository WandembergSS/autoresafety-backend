package com.autoresafety.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.autoresafety.api.dto.ProjectNamePayload;
import com.autoresafety.api.dto.ProjectPayload;
import com.autoresafety.api.dto.ProjectResumeDto;
import com.autoresafety.api.dto.ProjectStatusUpdatePayload;
import com.autoresafety.api.dto.StepFourProjectUpdatePayload;
import com.autoresafety.api.dto.StepFiveProjectInformationDto;
import com.autoresafety.api.dto.StepFiveProjectUpdatePayload;
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

    private static final Pattern STEP5_LOSS_SCENARIO_ID_PATTERN = Pattern.compile("^LS-(\\d{2,})$");
    private static final Pattern STEP5_SAFETY_REQUIREMENT_ID_PATTERN = Pattern.compile("^SR-(\\d{2,})$");

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

    @GET
    @Path("/step_four_project_information/{id}")
    public ProjectDocumentDto.Step4UcasDto getStepFourInformation(@PathParam("id") Long id) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        if (document == null) {
            throw new NotFoundException();
        }

        if (document.getStep4Ucas() == null) {
            return ProjectDocumentDto.Step4UcasDto.builder()
                .ucas(null)
                .hazardousConditions(null)
                .controllerConstraints(null)
                .build();
        }

        return document.getStep4Ucas();
    }

    @GET
    @Path("/step_five_project_information/{id}")
    public StepFiveProjectInformationDto getStepFiveInformation(@PathParam("id") Long id) {
        repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        return buildStepFiveInformation(id, document);
    }

    @POST
    @Path("/step_five_project_update")
    @Transactional
    public StepFiveProjectInformationDto updateStepFive(@Valid StepFiveProjectUpdatePayload payload) {
        Long projectId = payload == null ? null : payload.id();
        if (projectId == null) {
            throw new BadRequestException("id is required");
        }

        repository.findByIdOptional(projectId).orElseThrow(NotFoundException::new);

        ProjectDocumentDto document = projectDocumentService.getByProjectId(projectId);
        if (document == null) {
            document = ProjectDocumentDto.builder().build();
        }

        List<StepFiveProjectInformationDto.StepFiveUnsafeBehavior> unsafeBehaviors =
            buildStepFiveUnsafeBehaviors(document.getStep4Ucas());
        Set<String> availableUnsafeBehaviorIds = new LinkedHashSet<>();
        for (StepFiveProjectInformationDto.StepFiveUnsafeBehavior unsafeBehavior : unsafeBehaviors) {
            if (unsafeBehavior != null && unsafeBehavior.id() != null) {
                availableUnsafeBehaviorIds.add(unsafeBehavior.id());
            }
        }

        validateStepFivePayload(payload, availableUnsafeBehaviorIds);

        List<ProjectDocumentDto.Step5InformationDto.LossScenarioDto> mappedLossScenarios = new ArrayList<>();
        List<StepFiveProjectUpdatePayload.LossScenarioPayload> requestedLossScenarios =
            payload.step5Information().lossScenarios();
        if (requestedLossScenarios != null) {
            for (StepFiveProjectUpdatePayload.LossScenarioPayload lossScenario : requestedLossScenarios) {
                if (lossScenario == null) {
                    continue;
                }

                mappedLossScenarios.add(ProjectDocumentDto.Step5InformationDto.LossScenarioDto.builder()
                    .id(lossScenario.id().trim())
                    .description(lossScenario.description().trim())
                    .associatedUnsafeBehaviorIds(toTrimmedList(lossScenario.associatedUnsafeBehaviorIds()))
                    .sourceRationale(trimToNull(lossScenario.sourceRationale()))
                    .build());
            }
        }

        List<ProjectDocumentDto.Step5InformationDto.SafetyRequirementDto> mappedSafetyRequirements = new ArrayList<>();
        List<StepFiveProjectUpdatePayload.SafetyRequirementPayload> requestedSafetyRequirements =
            payload.step5Information().safetyRequirements();
        if (requestedSafetyRequirements != null) {
            for (StepFiveProjectUpdatePayload.SafetyRequirementPayload safetyRequirement : requestedSafetyRequirements) {
                if (safetyRequirement == null) {
                    continue;
                }

                mappedSafetyRequirements.add(ProjectDocumentDto.Step5InformationDto.SafetyRequirementDto.builder()
                    .id(safetyRequirement.id().trim())
                    .description(safetyRequirement.description().trim())
                    .addressedLossScenarioIds(toTrimmedList(safetyRequirement.addressedLossScenarioIds()))
                    .build());
            }
        }

        ProjectDocumentDto.Step5InformationDto persistedStep5 = ProjectDocumentDto.Step5InformationDto.builder()
            .lossScenarios(mappedLossScenarios)
            .safetyRequirements(mappedSafetyRequirements)
            .build();
        document.setStep5Information(persistedStep5);

        projectDocumentService.saveOrUpdate(projectId, document);
        return buildStepFiveInformation(projectId, document);
    }

    @GET
    @Path("/step_six_project_information/{id}")
    public ProjectDocumentDto.Step6LossScenariosDto getStepSixInformation(@PathParam("id") Long id) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(id);
        if (document == null) {
            throw new NotFoundException();
        }

        if (document.getStep6LossScenarios() == null) {
            return ProjectDocumentDto.Step6LossScenariosDto.builder()
                .lossScenarios(null)
                .safetyRequirements(null)
                .build();
        }

        return document.getStep6LossScenarios();
    }

    @GET
    @Path("/step_three_project_export/{id}/json")
    public Response exportStepThreeJson(@PathParam("id") Long id) {
        ProjectDocumentDto.Step3ControlStructureDto step3 = getStepThreeInformation(id);
        return Response.ok(step3)
            .type(MediaType.APPLICATION_JSON)
            .header("Content-Disposition", "attachment; filename=project-" + id + "-step3-control-structure.json")
            .build();
    }

    @GET
    @Path("/step_three_project_export/{id}/image")
    @Produces("image/svg+xml")
    public Response exportStepThreeImage(@PathParam("id") Long id) {
        ProjectDocumentDto.Step3ControlStructureDto step3 = getStepThreeInformation(id);
        String svg = buildStepThreeSvg(step3);

        return Response.ok(svg)
            .type("image/svg+xml")
            .header("Content-Disposition", "attachment; filename=project-" + id + "-step3-control-structure.svg")
            .build();
    }

    @POST
    @Path("/step_four_project_update")
    @Transactional
    public Response updateStepFour(@Valid StepFourProjectUpdatePayload payload) {
        return updateStepFourById(payload.id(), payload.step4Information());
    }

    @POST
    @Path("/step_four_project_update/{id}")
    @Transactional
    public Response updateStepFourByPath(@PathParam("id") Long id, @Valid StepFourProjectUpdatePayload payload) {
        Long payloadId = payload == null ? null : payload.id();
        if (payloadId != null && !payloadId.equals(id)) {
            throw new BadRequestException("Path id does not match payload id");
        }

        Long effectiveId = payloadId == null ? id : payloadId;
        StepFourProjectUpdatePayload.Step4Information step4Information = payload == null ? null : payload.step4Information();
        return updateStepFourById(effectiveId, step4Information);
    }

    private Response updateStepFourById(Long projectId, StepFourProjectUpdatePayload.Step4Information step4Information) {
        ProjectDocumentDto document = projectDocumentService.getByProjectId(projectId);
        if (document == null) {
            throw new NotFoundException();
        }

        ProjectDocumentDto.Step4UcasDto current = document.getStep4Ucas();
        if (current == null) {
            current = ProjectDocumentDto.Step4UcasDto.builder().build();
        }

        if (step4Information != null) {
            if (step4Information.unsafeControlActions() != null) {
                List<ProjectDocumentDto.Step4UcasDto.UcaDto> mappedUcas = new ArrayList<>();
                for (StepFourProjectUpdatePayload.UcaPayload uca : step4Information.unsafeControlActions()) {
                    if (uca == null) {
                        continue;
                    }

                    List<String> hazardRefs = uca.hazardRefs();
                    if ((hazardRefs == null || hazardRefs.isEmpty()) && uca.hazard() != null && !uca.hazard().isBlank()) {
                        hazardRefs = List.of(uca.hazard());
                    }

                    mappedUcas.add(ProjectDocumentDto.Step4UcasDto.UcaDto.builder()
                        .id(uca.id())
                        .ref(uca.ref())
                        .controlActionRef(uca.controlActionRef())
                        .sourceActor(uca.sourceActor())
                        .targetActor(uca.targetActor())
                        .controller(uca.controller())
                        .controlAction(uca.controlAction())
                        .controlledProcess(uca.controlledProcess())
                        .hazard(uca.hazard())
                        .category(uca.category())
                        .context(uca.context())
                        .consequence(uca.consequence())
                        .rationale(uca.rationale())
                        .hazardRefs(hazardRefs)
                        .responsibilityId(uca.responsibilityId())
                        .safetyConstraintId(uca.safetyConstraintId())
                        .build());
                }
                current.setUcas(mappedUcas);
            }

            if (step4Information.hazardousConditions() != null) {
                List<ProjectDocumentDto.Step4UcasDto.HazardousConditionDto> mappedHazardousConditions = new ArrayList<>();
                for (StepFourProjectUpdatePayload.HazardousConditionPayload condition : step4Information.hazardousConditions()) {
                    if (condition == null) {
                        continue;
                    }

                    mappedHazardousConditions.add(ProjectDocumentDto.Step4UcasDto.HazardousConditionDto.builder()
                        .id(condition.id())
                        .ref(condition.ref())
                        .description(condition.description())
                        .context(condition.context())
                        .consequence(condition.consequence())
                        .hazardRefs(condition.hazardRefs())
                        .build());
                }
                current.setHazardousConditions(mappedHazardousConditions);
            }

            if (step4Information.controllerConstraints() != null) {
                List<ProjectDocumentDto.Step4UcasDto.ControllerConstraintDto> mappedConstraints = new ArrayList<>();
                for (StepFourProjectUpdatePayload.ControllerConstraintPayload constraint : step4Information.controllerConstraints()) {
                    if (constraint == null) {
                        continue;
                    }

                    mappedConstraints.add(ProjectDocumentDto.Step4UcasDto.ControllerConstraintDto.builder()
                        .id(constraint.id())
                        .sourceUcaHc(constraint.sourceUcaHc())
                        .constraintId(constraint.constraintId())
                        .constraintStatement(constraint.constraintStatement())
                        .build());
                }
                current.setControllerConstraints(mappedConstraints);
            }
        }

        document.setStep4Ucas(current);
        projectDocumentService.saveOrUpdate(projectId, document);
        return Response.ok().build();
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

    private StepFiveProjectInformationDto buildStepFiveInformation(Long projectId, ProjectDocumentDto document) {
        List<StepFiveProjectInformationDto.StepFiveUnsafeBehavior> unsafeBehaviors =
            buildStepFiveUnsafeBehaviors(document == null ? null : document.getStep4Ucas());

        ProjectDocumentDto.Step5InformationDto step5 = document == null ? null : document.getStep5Information();
        List<StepFiveProjectInformationDto.StepFiveLossScenario> lossScenarios = new ArrayList<>();
        List<StepFiveProjectInformationDto.StepFiveSafetyRequirement> safetyRequirements = new ArrayList<>();

        if (step5 != null && step5.getLossScenarios() != null) {
            for (ProjectDocumentDto.Step5InformationDto.LossScenarioDto lossScenario : step5.getLossScenarios()) {
                if (lossScenario == null) {
                    continue;
                }

                lossScenarios.add(new StepFiveProjectInformationDto.StepFiveLossScenario(
                    trimToNull(lossScenario.getId()),
                    trimToNull(lossScenario.getDescription()),
                    toTrimmedList(lossScenario.getAssociatedUnsafeBehaviorIds()),
                    trimToNull(lossScenario.getSourceRationale())
                ));
            }
        }

        if (step5 != null && step5.getSafetyRequirements() != null) {
            for (ProjectDocumentDto.Step5InformationDto.SafetyRequirementDto safetyRequirement : step5.getSafetyRequirements()) {
                if (safetyRequirement == null) {
                    continue;
                }

                safetyRequirements.add(new StepFiveProjectInformationDto.StepFiveSafetyRequirement(
                    trimToNull(safetyRequirement.getId()),
                    trimToNull(safetyRequirement.getDescription()),
                    toTrimmedList(safetyRequirement.getAddressedLossScenarioIds())
                ));
            }
        }

        StepFiveProjectInformationDto.Defaults defaults = new StepFiveProjectInformationDto.Defaults(
            nextGeneratedStep5Id("LS", lossScenarios.stream().map(StepFiveProjectInformationDto.StepFiveLossScenario::id).toList()),
            nextGeneratedStep5Id("SR", safetyRequirements.stream().map(StepFiveProjectInformationDto.StepFiveSafetyRequirement::id).toList())
        );

        return new StepFiveProjectInformationDto(
            projectId,
            5,
            new StepFiveProjectInformationDto.AvailableInputs(unsafeBehaviors),
            new StepFiveProjectInformationDto.CurrentData(lossScenarios, safetyRequirements),
            defaults
        );
    }

    private List<StepFiveProjectInformationDto.StepFiveUnsafeBehavior> buildStepFiveUnsafeBehaviors(
        ProjectDocumentDto.Step4UcasDto step4
    ) {
        List<StepFiveProjectInformationDto.StepFiveUnsafeBehavior> unsafeBehaviors = new ArrayList<>();
        if (step4 == null) {
            return unsafeBehaviors;
        }

        int ucaIndex = 1;
        if (step4.getUcas() != null) {
            for (ProjectDocumentDto.Step4UcasDto.UcaDto uca : step4.getUcas()) {
                if (uca == null) {
                    continue;
                }

                String id = firstNonBlank(uca.getRef(), formatGeneratedId("UCA", ucaIndex));
                String description = firstNonBlank(
                    uca.getRationale(),
                    describeUcaFallback(uca),
                    id
                );
                List<String> hazards = collectHazards(uca.getHazardRefs(), uca.getHazard());

                unsafeBehaviors.add(new StepFiveProjectInformationDto.StepFiveUnsafeBehavior(
                    id,
                    "UCA",
                    id,
                    description,
                    hazards
                ));
                ucaIndex++;
            }
        }

        int hcIndex = 1;
        if (step4.getHazardousConditions() != null) {
            for (ProjectDocumentDto.Step4UcasDto.HazardousConditionDto hazardousCondition : step4.getHazardousConditions()) {
                if (hazardousCondition == null) {
                    continue;
                }

                String id = firstNonBlank(hazardousCondition.getRef(), formatGeneratedId("HC", hcIndex));
                String description = firstNonBlank(
                    hazardousCondition.getDescription(),
                    hazardousCondition.getConsequence(),
                    id
                );

                unsafeBehaviors.add(new StepFiveProjectInformationDto.StepFiveUnsafeBehavior(
                    id,
                    "HC",
                    id,
                    description,
                    toTrimmedList(hazardousCondition.getHazardRefs())
                ));
                hcIndex++;
            }
        }

        return unsafeBehaviors;
    }

    private static void validateStepFivePayload(
        StepFiveProjectUpdatePayload payload,
        Set<String> availableUnsafeBehaviorIds
    ) {
        if (payload == null || payload.step5Information() == null) {
            throw new BadRequestException("step5Information is required");
        }

        Set<String> lossScenarioIds = new LinkedHashSet<>();
        List<StepFiveProjectUpdatePayload.LossScenarioPayload> lossScenarios = payload.step5Information().lossScenarios();
        if (lossScenarios != null) {
            for (StepFiveProjectUpdatePayload.LossScenarioPayload lossScenario : lossScenarios) {
                if (lossScenario == null) {
                    continue;
                }

                String lossScenarioId = trimToNull(lossScenario.id());
                if (lossScenarioId == null || !STEP5_LOSS_SCENARIO_ID_PATTERN.matcher(lossScenarioId).matches()) {
                    throw new BadRequestException("lossScenarios[].id must match LS-XX");
                }
                if (!lossScenarioIds.add(lossScenarioId)) {
                    throw new BadRequestException("Duplicate loss scenario id: " + lossScenarioId);
                }
                if (trimToNull(lossScenario.description()) == null) {
                    throw new BadRequestException("lossScenarios[].description is required");
                }

                List<String> associatedUnsafeBehaviorIds = toTrimmedList(lossScenario.associatedUnsafeBehaviorIds());
                for (String associatedUnsafeBehaviorId : associatedUnsafeBehaviorIds) {
                    if (!availableUnsafeBehaviorIds.contains(associatedUnsafeBehaviorId)) {
                        throw new BadRequestException(
                            "lossScenarios[].associatedUnsafeBehaviorIds contains unknown id: " + associatedUnsafeBehaviorId
                        );
                    }
                }
            }
        }

        List<StepFiveProjectUpdatePayload.SafetyRequirementPayload> safetyRequirements = payload.step5Information().safetyRequirements();
        if (safetyRequirements != null) {
            for (StepFiveProjectUpdatePayload.SafetyRequirementPayload safetyRequirement : safetyRequirements) {
                if (safetyRequirement == null) {
                    continue;
                }

                String safetyRequirementId = trimToNull(safetyRequirement.id());
                if (safetyRequirementId == null || !STEP5_SAFETY_REQUIREMENT_ID_PATTERN.matcher(safetyRequirementId).matches()) {
                    throw new BadRequestException("safetyRequirements[].id must match SR-XX");
                }
                if (trimToNull(safetyRequirement.description()) == null) {
                    throw new BadRequestException("safetyRequirements[].description is required");
                }

                List<String> addressedLossScenarioIds = toTrimmedList(safetyRequirement.addressedLossScenarioIds());
                for (String addressedLossScenarioId : addressedLossScenarioIds) {
                    if (!lossScenarioIds.contains(addressedLossScenarioId)) {
                        throw new BadRequestException(
                            "safetyRequirements[].addressedLossScenarioIds contains unknown id: " + addressedLossScenarioId
                        );
                    }
                }
            }
        }
    }

    private static List<String> collectHazards(List<String> hazardRefs, String hazard) {
        LinkedHashSet<String> hazards = new LinkedHashSet<>(toTrimmedList(hazardRefs));
        String normalizedHazard = trimToNull(hazard);
        if (normalizedHazard != null) {
            hazards.add(normalizedHazard);
        }
        return new ArrayList<>(hazards);
    }

    private static String describeUcaFallback(ProjectDocumentDto.Step4UcasDto.UcaDto uca) {
        String controller = trimToNull(uca.getController());
        String controlAction = trimToNull(uca.getControlAction());
        String category = trimToNull(uca.getCategory());

        if (controller != null && controlAction != null && category != null) {
            return controller + " " + controlAction + " (" + category + ")";
        }
        if (controller != null && controlAction != null) {
            return controller + " " + controlAction;
        }
        return firstNonBlank(controller, controlAction, category);
    }

    private static String formatGeneratedId(String prefix, int index) {
        return prefix + "-" + String.format("%02d", index);
    }

    private static String nextGeneratedStep5Id(String prefix, List<String> ids) {
        Pattern pattern = "LS".equals(prefix) ? STEP5_LOSS_SCENARIO_ID_PATTERN : STEP5_SAFETY_REQUIREMENT_ID_PATTERN;
        int max = 0;
        int width = 2;

        if (ids != null) {
            for (String id : ids) {
                String normalizedId = trimToNull(id);
                if (normalizedId == null) {
                    continue;
                }
                Matcher matcher = pattern.matcher(normalizedId);
                if (!matcher.matches()) {
                    continue;
                }

                String numericPart = matcher.group(1);
                width = Math.max(width, numericPart.length());
                int parsed = Integer.parseInt(numericPart);
                max = Math.max(max, parsed);
            }
        }

        int next = max + 1;
        return prefix + "-" + String.format("%0" + width + "d", next);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private static List<String> toTrimmedList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalizedValues = new ArrayList<>();
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                normalizedValues.add(normalized);
            }
        }
        return normalizedValues;
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

    private static String buildStepThreeSvg(ProjectDocumentDto.Step3ControlStructureDto step3) {
        Set<String> nodes = new LinkedHashSet<>();
        List<ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto> actions =
            step3 == null ? null : step3.getControlActions();
        List<ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto> feedbacks =
            step3 == null ? null : step3.getFeedbackLoops();

        if (actions != null) {
            for (ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto action : actions) {
                if (action == null) {
                    continue;
                }
                if (action.getController() != null && !action.getController().isBlank()) {
                    nodes.add(action.getController().trim());
                }
                if (action.getControlledProcess() != null && !action.getControlledProcess().isBlank()) {
                    nodes.add(action.getControlledProcess().trim());
                }
            }
        }

        if (feedbacks != null) {
            for (ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto feedback : feedbacks) {
                if (feedback == null) {
                    continue;
                }
                if (feedback.getSource() != null && !feedback.getSource().isBlank()) {
                    nodes.add(feedback.getSource().trim());
                }
                if (feedback.getDestination() != null && !feedback.getDestination().isBlank()) {
                    nodes.add(feedback.getDestination().trim());
                }
            }
        }

        int nodeCount = Math.max(nodes.size(), 1);
        int width = Math.max(900, 220 * nodeCount);
        int height = 520;
        int boxWidth = 170;
        int boxHeight = 56;
        int leftPadding = 40;
        int topRowY = 90;
        int bottomRowY = 320;
        int spacing = nodeCount == 1 ? 0 : (width - (2 * leftPadding) - boxWidth) / (nodeCount - 1);

        List<String> nodeList = new ArrayList<>(nodes);
        if (nodeList.isEmpty()) {
            nodeList.add("No control structure data");
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(width)
            .append("' height='").append(height).append("' viewBox='0 0 ").append(width)
            .append(" ").append(height).append("'>");
        svg.append("<rect width='100%' height='100%' fill='white'/>");
        svg.append("<text x='30' y='40' font-size='22' font-family='Arial' fill='#1F2937'>System Safety Control Structure</text>");

        for (int i = 0; i < nodeList.size(); i++) {
            int x = leftPadding + (spacing * i);
            int y = i % 2 == 0 ? topRowY : bottomRowY;

            svg.append("<rect x='").append(x).append("' y='").append(y)
                .append("' width='").append(boxWidth).append("' height='").append(boxHeight)
                .append("' rx='8' ry='8' fill='#EFF6FF' stroke='#2563EB' stroke-width='1.5'/>");
            svg.append("<text x='").append(x + 10).append("' y='").append(y + 33)
                .append("' font-size='13' font-family='Arial' fill='#1E3A8A'>")
                .append(escapeSvg(nodeList.get(i))).append("</text>");
        }

        if (actions != null) {
            for (ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto action : actions) {
                if (action == null || action.getController() == null || action.getControlledProcess() == null) {
                    continue;
                }
                int sourceIndex = nodeList.indexOf(action.getController().trim());
                int targetIndex = nodeList.indexOf(action.getControlledProcess().trim());
                if (sourceIndex < 0 || targetIndex < 0) {
                    continue;
                }

                int sx = leftPadding + (spacing * sourceIndex) + boxWidth;
                int sy = (sourceIndex % 2 == 0 ? topRowY : bottomRowY) + (boxHeight / 2);
                int tx = leftPadding + (spacing * targetIndex);
                int ty = (targetIndex % 2 == 0 ? topRowY : bottomRowY) + (boxHeight / 2);
                svg.append("<line x1='").append(sx).append("' y1='").append(sy)
                    .append("' x2='").append(tx).append("' y2='").append(ty)
                    .append("' stroke='#0F766E' stroke-width='2' marker-end='url(#arrow)'/>");
            }
        }

        svg.append("<defs><marker id='arrow' markerWidth='10' markerHeight='7' refX='9' refY='3.5' orient='auto'>")
            .append("<polygon points='0 0, 10 3.5, 0 7' fill='#0F766E'/></marker></defs>");
        svg.append("</svg>");
        return svg.toString();
    }

    private static String escapeSvg(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
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
