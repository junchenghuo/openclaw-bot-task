package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.Task;
import com.example.taskcenter.model.TaskPriority;
import com.example.taskcenter.model.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String taskCode,
        Long projectId,
        Long parentTaskId,
        String title,
        String taskType,
        TaskStatus status,
        TaskPriority priority,
        String detail,
        String initiator,
        String ownerName,
        String blockerContact,
        String blockReason,
        LocalDateTime plannedFinishAt,
        LocalDateTime actualStartAt,
        LocalDateTime actualFinishAt,
        String input,
        String output,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTaskCode(),
                task.getProject().getId(),
                task.getParentTask() == null ? null : task.getParentTask().getId(),
                task.getTitle(),
                task.getTaskType(),
                task.getStatus(),
                task.getPriority(),
                task.getDetail(),
                task.getInitiator(),
                task.getOwnerName(),
                task.getBlockerContact(),
                task.getBlockReason(),
                task.getPlannedFinishAt(),
                task.getActualStartAt(),
                task.getActualFinishAt(),
                task.getInputJson(),
                task.getOutputJson(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
