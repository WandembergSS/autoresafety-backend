package com.autoresafety.persistence;

import com.autoresafety.domain.Project;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProjectRepository implements PanacheRepository<Project> {
}
