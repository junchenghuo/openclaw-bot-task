package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class StartTaskRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
