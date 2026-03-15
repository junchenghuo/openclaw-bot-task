package com.example.taskcenter.service;

import com.example.taskcenter.entity.OutboxEvent;
import com.example.taskcenter.config.MattermostProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OutboxDispatchWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatchWorker.class);

    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final MattermostClient mattermostClient;
    private final MattermostProperties mattermostProperties;

    public OutboxDispatchWorker(OutboxEventService outboxEventService,
                                ObjectMapper objectMapper,
                                MattermostClient mattermostClient,
                                MattermostProperties mattermostProperties) {
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
        this.mattermostClient = mattermostClient;
        this.mattermostProperties = mattermostProperties;
    }

    @Scheduled(fixedDelayString = "${outbox.dispatch.fixed-delay-ms:5000}")
    public void dispatch() {
        List<OutboxEvent> pending = outboxEventService.pollPendingEvents();
        for (OutboxEvent e : pending) {
            try {
                outboxEventService.markProcessing(e.getId());
                String postId = sendToMattermost(e);
                outboxEventService.markSent(e.getId(), postId);
                log.info("outbox sent id={} type={} taskId={} postId={}", e.getId(), e.getEventType(), e.getTaskId(), postId);
            } catch (Exception ex) {
                OutboxEvent failed = outboxEventService.markFailed(e.getId(), ex.getMessage());
                log.warn("outbox failed id={} type={} taskId={} err={}", e.getId(), e.getEventType(), e.getTaskId(), ex.getMessage());
                if (failed.getStatus() == com.example.taskcenter.model.OutboxStatus.已取消) {
                    sendFailureAlert(failed);
                }
            }
        }
    }

    private String sendToMattermost(OutboxEvent e) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(e.getPayloadJson(), new TypeReference<>() {
        });
        String channelId = stringVal(payload.get("channelId"));
        String message = stringVal(payload.get("message"));
        if (channelId.isBlank() || message.isBlank()) {
            throw new IllegalArgumentException("payload 缺少 channelId/message");
        }
        @SuppressWarnings("unchecked")
        List<String> fileIds = payload.get("fileIds") instanceof List<?> raw
                ? raw.stream().map(String::valueOf).toList()
                : List.of();

        return mattermostClient.createPost(channelId, message, fileIds);
    }

    private String stringVal(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private void sendFailureAlert(OutboxEvent failed) {
        try {
            String channelId = mattermostProperties.getAlertChannelId();
            if (channelId == null || channelId.isBlank()) {
                return;
            }
            String mentions = mattermostProperties.getAlertMentions() == null ? "@bot-leader @admin" : mattermostProperties.getAlertMentions();
            String msg = mentions + "\n"
                    + "【Outbox失败告警】事件重试已耗尽，需人工介入\n"
                    + "事件ID: " + failed.getId() + "\n"
                    + "事件类型: " + failed.getEventType() + "\n"
                    + "任务ID: " + failed.getTaskId() + "\n"
                    + "项目ID: " + failed.getProjectId() + "\n"
                    + "目标频道: " + failed.getChannelId() + "\n"
                    + "最后错误: " + failed.getLastError();
            mattermostClient.createPost(channelId, msg, List.of());
        } catch (Exception ex) {
            log.warn("send outbox alert failed id={} err={}", failed.getId(), ex.getMessage());
        }
    }
}
