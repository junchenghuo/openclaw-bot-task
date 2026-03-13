package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String projectCode,
        String projectName,
        String status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getStatus(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
