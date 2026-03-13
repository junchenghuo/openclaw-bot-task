package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.TaskLog;

import java.time.LocalDateTime;

public record TaskLogResponse(
        Long id,
        Long taskId,
        String logType,
        String message,
        String payload,
        LocalDateTime createdAt
) {
    public static TaskLogResponse from(TaskLog log) {
        return new TaskLogResponse(
                log.getId(),
                log.getTask().getId(),
                log.getLogType(),
                log.getMessage(),
                log.getPayloadJson(),
                log.getCreatedAt()
        );
    }
}
