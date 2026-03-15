package com.example.taskcenter.service;

import com.example.taskcenter.dto.request.CompleteTaskRequest;
import com.example.taskcenter.dto.request.CreateTaskAndDispatchRequest;
import com.example.taskcenter.dto.request.SubmitDeliveryAndCompleteRequest;
import com.example.taskcenter.entity.OutboxEvent;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.entity.Task;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.example.taskcenter.model.OutboxEventType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskAtomicService {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final OutboxEventService outboxEventService;
    private final MattermostClient mattermostClient;

    public TaskAtomicService(TaskService taskService,
                             ProjectService projectService,
                             OutboxEventService outboxEventService,
                             MattermostClient mattermostClient) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.outboxEventService = outboxEventService;
        this.mattermostClient = mattermostClient;
    }

    @Transactional
    public AtomicCreateResult createTaskAndDispatch(CreateTaskAndDispatchRequest request) {
        Project project = projectService.getProject(request.getTask().getProjectId());
        String channelId = request.getChannelId() == null ? "" : request.getChannelId().trim();
        if (channelId.isBlank()) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "channelId 不能为空", HttpStatus.BAD_REQUEST);
        }

        // 先做发帖权限预检，避免任务已建但消息一定失败。
        mattermostClient.ensureCanPost(channelId);

        Task task = taskService.createTask(request.getTask());

        Map<String, Object> payload = new HashMap<>();
        payload.put("channelId", channelId);
        payload.put("account", "pm");
        payload.put("message", request.getDispatchMessage());
        payload.put("taskCode", task.getTaskCode());
        payload.put("taskId", task.getId());
        payload.put("projectId", project.getId());

        OutboxEvent event = outboxEventService.createEvent(
                OutboxEventType.任务指派,
                task.getId(),
                project.getId(),
                channelId,
                request.getIdempotencyKey(),
                payload
        );

        return new AtomicCreateResult(task, event);
    }

    @Transactional
    public AtomicCompleteResult submitDeliveryAndComplete(Long taskId, SubmitDeliveryAndCompleteRequest request) {
        if (request.getFileIds() == null || request.getFileIds().isEmpty()) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "fileIds 不能为空", HttpStatus.BAD_REQUEST);
        }

        validateFileIdsAndOutput(request.getFileIds(), request.getOutput());

        mattermostClient.ensureCanPost(request.getChannelId());

        CompleteTaskRequest complete = new CompleteTaskRequest();
        complete.setOperatorName(request.getOperatorName());
        complete.setOutput(request.getOutput());
        Task completed = taskService.completeTask(taskId, complete);

        Map<String, Object> payload = new HashMap<>();
        payload.put("channelId", request.getChannelId());
        payload.put("account", "pm");
        payload.put("message", request.getCompletionMessage());
        payload.put("taskCode", completed.getTaskCode());
        payload.put("taskId", completed.getId());
        payload.put("projectId", completed.getProject().getId());
        payload.put("fileIds", request.getFileIds());

        OutboxEvent event = outboxEventService.createEvent(
                OutboxEventType.任务完成通知,
                completed.getId(),
                completed.getProject().getId(),
                request.getChannelId(),
                request.getIdempotencyKey(),
                payload
        );

        return new AtomicCompleteResult(completed, event);
    }

    private void validateFileIdsAndOutput(java.util.List<String> fileIds, JsonNode output) {
        for (String fid : fileIds) {
            if (fid == null || fid.isBlank() || fid.trim().length() < 8) {
                throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "fileIds 存在非法值", HttpStatus.BAD_REQUEST);
            }
        }

        if (output == null || output.isNull()) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "output 不能为空", HttpStatus.BAD_REQUEST);
        }

        if (!output.isObject()) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "output 必须是对象", HttpStatus.BAD_REQUEST);
        }

        JsonNode summary = output.get("summary");
        if (summary == null || summary.asText().isBlank()) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "output.summary 不能为空", HttpStatus.BAD_REQUEST);
        }
    }

    public record AtomicCreateResult(Task task, OutboxEvent outboxEvent) {
    }

    public record AtomicCompleteResult(Task task, OutboxEvent outboxEvent) {
    }
}
