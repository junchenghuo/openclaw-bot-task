package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CastMeetingVoteRequest {

    @NotBlank(message = "voterName 不能为空")
    private String voterName;

    private String voterRole;

    private String voterMention;

    @NotBlank(message = "optionKey 不能为空")
    private String optionKey;

    private String reason;

    public String getVoterName() {
        return voterName;
    }

    public void setVoterName(String voterName) {
        this.voterName = voterName;
    }

    public String getVoterRole() {
        return voterRole;
    }

    public void setVoterRole(String voterRole) {
        this.voterRole = voterRole;
    }

    public String getVoterMention() {
        return voterMention;
    }

    public void setVoterMention(String voterMention) {
        this.voterMention = voterMention;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
