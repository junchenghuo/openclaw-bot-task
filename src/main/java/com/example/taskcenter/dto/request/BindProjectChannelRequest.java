package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BindProjectChannelRequest {

    @NotBlank(message = "mattermostChannelId 不能为空")
    private String mattermostChannelId;

    private String mattermostChannelName;

    public String getMattermostChannelId() {
        return mattermostChannelId;
    }

    public void setMattermostChannelId(String mattermostChannelId) {
        this.mattermostChannelId = mattermostChannelId;
    }

    public String getMattermostChannelName() {
        return mattermostChannelName;
    }

    public void setMattermostChannelName(String mattermostChannelName) {
        this.mattermostChannelName = mattermostChannelName;
    }
}
