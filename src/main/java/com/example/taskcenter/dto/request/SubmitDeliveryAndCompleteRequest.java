package com.example.taskcenter.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SubmitDeliveryAndCompleteRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    @NotBlank(message = "channelId 不能为空")
    private String channelId;

    @NotBlank(message = "completionMessage 不能为空")
    private String completionMessage;

    @NotNull(message = "output 不能为空")
    private JsonNode output;

    @NotEmpty(message = "fileIds 不能为空")
    private List<String> fileIds;

    @NotBlank(message = "idempotencyKey 不能为空")
    private String idempotencyKey;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCompletionMessage() {
        return completionMessage;
    }

    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }

    public JsonNode getOutput() {
        return output;
    }

    public void setOutput(JsonNode output) {
        this.output = output;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
