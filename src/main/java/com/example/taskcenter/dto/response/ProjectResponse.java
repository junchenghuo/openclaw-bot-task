package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String projectCode,
        String projectName,
        String status,
        String description,
        String workspacePath,
        String memoryPath,
        String mattermostChannelId,
        String mattermostChannelName,
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
                project.getWorkspacePath(),
                project.getMemoryPath(),
                project.getMattermostChannelId(),
                project.getMattermostChannelName(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
