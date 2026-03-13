package com.example.taskcenter.service;

import com.example.taskcenter.entity.Project;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.example.taskcenter.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    public Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        "Project not found: " + projectId,
                        HttpStatus.NOT_FOUND
                ));
    }
}
