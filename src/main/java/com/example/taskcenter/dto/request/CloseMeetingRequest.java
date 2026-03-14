package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CloseMeetingRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    @NotBlank(message = "decisionSummary 不能为空")
    private String decisionSummary;

    private String decisionOption;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDecisionSummary() {
        return decisionSummary;
    }

    public void setDecisionSummary(String decisionSummary) {
        this.decisionSummary = decisionSummary;
    }

    public String getDecisionOption() {
        return decisionOption;
    }

    public void setDecisionOption(String decisionOption) {
        this.decisionOption = decisionOption;
    }
}
