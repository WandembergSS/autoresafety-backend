package com.autoresafety.persistence;

import com.autoresafety.domain.ProjectDocument;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProjectDocumentRepository implements PanacheRepository<ProjectDocument> {
}