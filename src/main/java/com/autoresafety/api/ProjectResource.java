package com.autoresafety.api;

import java.net.URI;
import java.util.List;

import com.autoresafety.api.dto.ProjectNamePayload;
import com.autoresafety.api.dto.ProjectPayload;
import com.autoresafety.api.dto.ProjectResumeDto;
import com.autoresafety.api.dto.ProjectStatusUpdatePayload;
import com.autoresafety.api.dto.StepOneProjectInformationDto;
import com.autoresafety.api.dto.StepOneProjectUpdatePayload;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class ProjectResource {

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

    private static String formatObjectives(String objectives) {
        if (objectives == null || objectives.isBlank()) {
            return null;
        }
        return objectives.trim();
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
