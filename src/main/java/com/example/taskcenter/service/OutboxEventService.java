package com.example.taskcenter.service;

import com.example.taskcenter.entity.OutboxEvent;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.example.taskcenter.model.OutboxEventType;
import com.example.taskcenter.model.OutboxStatus;
import com.example.taskcenter.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OutboxEvent createEvent(OutboxEventType eventType,
                                   Long taskId,
                                   Long projectId,
                                   String channelId,
                                   String idempotencyKey,
                                   Map<String, Object> payload) {
        if (outboxEventRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BusinessException(
                    ErrorCodes.INVALID_ARGUMENT,
                    "idempotencyKey 重复: " + idempotencyKey,
                    HttpStatus.BAD_REQUEST
            );
        }

        OutboxEvent event = new OutboxEvent();
        event.setEventType(eventType);
        event.setStatus(OutboxStatus.待处理);
        event.setTaskId(taskId);
        event.setProjectId(projectId);
        event.setChannelId(channelId);
        event.setIdempotencyKey(idempotencyKey);
        event.setPayloadJson(toJson(payload));
        event.setAttemptCount(0);
        event.setMaxAttempts(10);
        event.setNextRetryAt(LocalDateTime.now());
        return outboxEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> pollPendingEvents() {
        return outboxEventRepository.findTop50ByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
                List.of(OutboxStatus.待处理, OutboxStatus.失败),
                LocalDateTime.now()
        );
    }

    @Transactional
    public OutboxEvent markProcessing(Long eventId) {
        OutboxEvent e = getById(eventId);
        e.setStatus(OutboxStatus.处理中);
        e.setAttemptCount(e.getAttemptCount() + 1);
        return outboxEventRepository.save(e);
    }

    @Transactional
    public OutboxEvent markSent(Long eventId, String externalMessageId) {
        OutboxEvent e = getById(eventId);
        e.setStatus(OutboxStatus.已发送);
        e.setSentAt(LocalDateTime.now());
        e.setExternalMessageId(externalMessageId);
        e.setLastError(null);
        return outboxEventRepository.save(e);
    }

    @Transactional
    public OutboxEvent markFailed(Long eventId, String errorMessage) {
        OutboxEvent e = getById(eventId);
        e.setLastError(errorMessage);

        if (e.getAttemptCount() >= e.getMaxAttempts()) {
            e.setStatus(OutboxStatus.已取消);
            e.setNextRetryAt(null);
        } else {
            e.setStatus(OutboxStatus.失败);
            // 指数退避上限 30 分钟
            int minute = (int) Math.min(30, Math.pow(2, Math.max(0, e.getAttemptCount() - 1)));
            e.setNextRetryAt(LocalDateTime.now().plusMinutes(minute));
        }

        return outboxEventRepository.save(e);
    }

    @Transactional(readOnly = true)
    public OutboxEvent getById(Long id) {
        return outboxEventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        "Outbox event not found: " + id,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> listRecent(int limit) {
        int size = Math.max(1, Math.min(200, limit));
        return outboxEventRepository.findByOrderByIdDesc(PageRequest.of(0, size));
    }

    @Transactional
    public OutboxEvent replay(Long id) {
        OutboxEvent e = getById(id);
        e.setStatus(OutboxStatus.待处理);
        e.setNextRetryAt(LocalDateTime.now());
        e.setLastError(null);
        return outboxEventRepository.save(e);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "outbox payload 序列化失败", HttpStatus.BAD_REQUEST);
        }
    }
}
