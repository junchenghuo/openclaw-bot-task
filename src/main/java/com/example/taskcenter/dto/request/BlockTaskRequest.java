package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BlockTaskRequest {

    @NotBlank(message = "operatorName 不能为空")
    private String operatorName;

    @NotBlank(message = "blockerContact 不能为空")
    private String blockerContact;

    @NotBlank(message = "blockReason 不能为空")
    private String blockReason;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getBlockerContact() {
        return blockerContact;
    }

    public void setBlockerContact(String blockerContact) {
        this.blockerContact = blockerContact;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }
}
