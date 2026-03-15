package com.example.taskcenter.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectRequest {

    @NotBlank(message = "projectCode 不能为空")
    private String projectCode;

    @NotBlank(message = "projectName 不能为空")
    private String projectName;

    private String status;

    private String description;

    private String mattermostChannelId;

    private String mattermostChannelName;

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
