package com.example.taskcenter.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateProjectMeetingRequest {

    @NotBlank(message = "topic 不能为空")
    private String topic;

    private String problemStatement;

    @NotBlank(message = "organizerName 不能为空")
    private String organizerName;

    private Long relatedTaskId;

    private LocalDateTime scheduledAt;

    @NotEmpty(message = "decisionOptions 不能为空")
    private List<String> decisionOptions = new ArrayList<>();

    @Valid
    private List<Participant> participants = new ArrayList<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public Long getRelatedTaskId() {
        return relatedTaskId;
    }

    public void setRelatedTaskId(Long relatedTaskId) {
        this.relatedTaskId = relatedTaskId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public List<String> getDecisionOptions() {
        return decisionOptions;
    }

    public void setDecisionOptions(List<String> decisionOptions) {
        this.decisionOptions = decisionOptions;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public static class Participant {
        @NotBlank(message = "participant.name 不能为空")
        private String name;
        private String role;
        private String mention;
        private String responsibility;

        public Participant() {
        }

        public Participant(String name, String role, String mention, String responsibility) {
            this.name = name;
            this.role = role;
            this.mention = mention;
            this.responsibility = responsibility;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getMention() {
            return mention;
        }

        public void setMention(String mention) {
            this.mention = mention;
        }

        public String getResponsibility() {
            return responsibility;
        }

        public void setResponsibility(String responsibility) {
            this.responsibility = responsibility;
        }
    }
}
