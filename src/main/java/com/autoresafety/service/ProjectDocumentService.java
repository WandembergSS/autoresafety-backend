package com.autoresafety.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.domain.Project;
import com.autoresafety.domain.ProjectDocument;
import com.autoresafety.persistence.ProjectDocumentRepository;
import com.autoresafety.persistence.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ProjectDocumentService {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectDocumentRepository projectDocumentRepository;

    public ProjectDocumentDto getByProjectId(Long projectId) {
        ProjectDocument doc = ProjectDocument.find("project.id", projectId).firstResult();
        return doc == null ? null : doc.document;
    }

    @Transactional
    public ProjectDocumentDto saveOrUpdate(Long projectId, ProjectDocumentDto dto) {
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }

        validateStepThreeControllers(dto);

        ProjectDocument doc = ProjectDocument.find("project.id", projectId).firstResult();
        if (doc == null) {
            doc = new ProjectDocument();
            doc.project = project;
        }

        doc.document = dto;
        projectDocumentRepository.persist(doc);
        return doc.document;
    }

    private static void validateStepThreeControllers(ProjectDocumentDto dto) {
        if (dto == null || dto.getStep3ControlStructure() == null || dto.getStep3ControlStructure().getControlActions() == null) {
            return;
        }

        Set<String> allowedControllers = new LinkedHashSet<>();
        if (dto.getStep1Scope() != null && dto.getStep1Scope().getSystemComponents() != null) {
            for (ProjectDocumentDto.Step1ScopeDto.SystemComponentDto component : dto.getStep1Scope().getSystemComponents()) {
                if (component == null || component.getName() == null) {
                    continue;
                }
                String normalizedName = component.getName().trim();
                if (!normalizedName.isEmpty()) {
                    allowedControllers.add(normalizedName);
                }
            }
        }

        Set<String> invalidControllers = new LinkedHashSet<>();
        List<ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto> controlActions = dto.getStep3ControlStructure().getControlActions();
        for (ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto action : controlActions) {
            if (action == null || action.getController() == null) {
                invalidControllers.add("(blank)");
                continue;
            }
            String normalizedController = action.getController().trim();
            if (normalizedController.isEmpty()) {
                invalidControllers.add("(blank)");
                continue;
            }
            if (!allowedControllers.contains(normalizedController)) {
                invalidControllers.add(normalizedController);
            }
        }

        if (!invalidControllers.isEmpty()) {
            throw new WebApplicationException(
                Response.status(422)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                        "message", "Step 3 controller must exist in Step 1 systemComponents",
                        "invalidControllers", invalidControllers
                    ))
                    .build()
            );
        }
    }
}
