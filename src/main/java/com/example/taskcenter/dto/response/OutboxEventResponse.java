package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.OutboxEvent;

import java.time.LocalDateTime;

public record OutboxEventResponse(
        Long id,
        String eventType,
        String status,
        Long taskId,
        Long projectId,
        String channelId,
        String idempotencyKey,
        Integer attemptCount,
        Integer maxAttempts,
        LocalDateTime nextRetryAt,
        LocalDateTime sentAt,
        String lastError,
        String externalMessageId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OutboxEventResponse from(OutboxEvent e) {
        return new OutboxEventResponse(
                e.getId(),
                e.getEventType() == null ? null : e.getEventType().name(),
                e.getStatus() == null ? null : e.getStatus().name(),
                e.getTaskId(),
                e.getProjectId(),
                e.getChannelId(),
                e.getIdempotencyKey(),
                e.getAttemptCount(),
                e.getMaxAttempts(),
                e.getNextRetryAt(),
                e.getSentAt(),
                e.getLastError(),
                e.getExternalMessageId(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
