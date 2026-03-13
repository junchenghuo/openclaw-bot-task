package com.example.taskcenter.dto.request;

import com.example.taskcenter.model.TaskPriority;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class UpdateTaskRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    private String title;
    private String taskType;
    private TaskPriority priority;
    private String detail;
    private String ownerName;
    private LocalDateTime plannedFinishAt;
    private JsonNode input;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
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
