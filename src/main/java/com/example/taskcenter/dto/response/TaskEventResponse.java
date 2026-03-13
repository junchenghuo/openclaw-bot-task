package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.TaskEvent;

import java.time.LocalDateTime;

public record TaskEventResponse(
        Long id,
        Long taskId,
        String eventType,
        String fromStatus,
        String toStatus,
        String operatorName,
        String note,
        LocalDateTime createdAt
) {
    public static TaskEventResponse from(TaskEvent event) {
        return new TaskEventResponse(
                event.getId(),
                event.getTask().getId(),
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getOperatorName(),
                event.getNote(),
                event.getCreatedAt()
        );
    }
}
