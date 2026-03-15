package com.example.taskcenter.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTaskAndDispatchRequest {

    @NotNull(message = "task 不能为空")
    @Valid
    private CreateTaskRequest task;

    @NotBlank(message = "channelId 不能为空")
    private String channelId;

    @NotBlank(message = "dispatchMessage 不能为空")
    private String dispatchMessage;

    @NotBlank(message = "idempotencyKey 不能为空")
    private String idempotencyKey;

    public CreateTaskRequest getTask() {
        return task;
    }

    public void setTask(CreateTaskRequest task) {
        this.task = task;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDispatchMessage() {
        return dispatchMessage;
    }

    public void setDispatchMessage(String dispatchMessage) {
        this.dispatchMessage = dispatchMessage;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
