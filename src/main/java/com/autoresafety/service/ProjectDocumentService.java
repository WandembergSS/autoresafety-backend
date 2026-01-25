package com.autoresafety.service;

import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.domain.Project;
import com.autoresafety.domain.ProjectDocument;
import com.autoresafety.persistence.ProjectDocumentRepository;
import com.autoresafety.persistence.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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

        ProjectDocument doc = ProjectDocument.find("project.id", projectId).firstResult();
        if (doc == null) {
            doc = new ProjectDocument();
            doc.project = project;
        }

        doc.document = dto;
        projectDocumentRepository.persist(doc);
        return doc.document;
    }
}
