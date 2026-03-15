package com.example.taskcenter.dto.request;

import com.example.taskcenter.model.TaskPriority;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateTaskRequest {

    @NotNull(message = "projectId 不能为空")
    private Long projectId;

    private Long parentTaskId;

    @NotBlank(message = "title 不能为空")
    private String title;

    @NotBlank(message = "taskType 不能为空")
    private String taskType;

    private TaskPriority priority = TaskPriority.中;

    private String detail;

    private String initiator;

    private String ownerName;

    private LocalDateTime plannedFinishAt;

    private JsonNode input;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public LocalDateTime getPlannedFinishAt() {
        return plannedFinishAt;
    }

    public void setPlannedFinishAt(LocalDateTime plannedFinishAt) {
        this.plannedFinishAt = plannedFinishAt;
    }

    public JsonNode getInput() {
        return input;
    }

    public void setInput(JsonNode input) {
        this.input = input;
    }
}
