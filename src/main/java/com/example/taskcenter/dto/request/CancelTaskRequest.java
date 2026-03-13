package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CancelTaskRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    @NotBlank(message = "reason 不能为空")
    private String reason;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
