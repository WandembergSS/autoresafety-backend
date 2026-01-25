package com.autoresafety.api;

import java.net.URI;
import java.util.List;

import com.autoresafety.api.dto.ProjectNamePayload;
import com.autoresafety.api.dto.ProjectPayload;
import com.autoresafety.api.dto.ProjectResumeDto;
import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.domain.Project;
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
