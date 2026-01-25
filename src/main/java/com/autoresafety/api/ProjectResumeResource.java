package com.autoresafety.api;

import com.autoresafety.api.dto.ProjectResumeDto;
import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.domain.Project;
import com.autoresafety.domain.ProjectDocument;
import com.autoresafety.persistence.ProjectDocumentRepository;
import com.autoresafety.persistence.ProjectRepository;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/project-resume")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class ProjectResumeResource {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectDocumentRepository projectDocumentRepository;

    @GET
    public List<ProjectResumeDto> listActiveResumes() {
        List<Project> projects = projectRepository.find("lower(status) <> ?1", Sort.by("name"), "complete").list();
        if (projects.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = projects.stream().map(p -> p.id).toList();
        List<ProjectDocument> documents = projectDocumentRepository.find("project.id in ?1", projectIds).list();

        Map<Long, ProjectDocumentDto.ProjectDto> projectInfoById = documents.stream()
                .filter(doc -> doc.document != null && doc.document.getProject() != null)
                .collect(Collectors.toMap(
                        doc -> doc.project.id,
                        doc -> doc.document.getProject(),
                        (left, right) -> left
                ));

        return projects.stream().map(project -> {
            ProjectDocumentDto.ProjectDto info = projectInfoById.get(project.id);
            return ProjectResumeDto.builder()
                    .id(project.id)
                    .name(project.name)
                    .description(project.description)
                    .status(project.status)
                    .domain(info == null ? null : info.getDomain())
                    .owner(info == null ? null : info.getOwner())
                    .currentStep(info == null ? null : info.getCurrentStep())
                    .build();
        }).toList();
    }
}
