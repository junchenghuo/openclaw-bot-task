package com.example.taskcenter.entity;

import com.example.taskcenter.model.MeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_meeting")
public class ProjectMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_code", nullable = false, unique = true, length = 64)
    private String meetingCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_task_id")
    private Task relatedTask;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(name = "problem_statement", columnDefinition = "TEXT")
    private String problemStatement;

    @Column(name = "organizer_name", nullable = false, length = 100)
    private String organizerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MeetingStatus status = MeetingStatus.VOTING;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "decision_option", length = 200)
    private String decisionOption;

    @Column(name = "decision_summary", columnDefinition = "TEXT")
    private String decisionSummary;

    @Column(name = "decision_options_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String decisionOptionsJson;

    @Column(name = "minutes_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String minutesJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeetingCode() {
        return meetingCode;
    }

    public void setMeetingCode(String meetingCode) {
        this.meetingCode = meetingCode;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Task getRelatedTask() {
        return relatedTask;
    }

    public void setRelatedTask(Task relatedTask) {
        this.relatedTask = relatedTask;
    }

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

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getDecisionOption() {
        return decisionOption;
    }

    public void setDecisionOption(String decisionOption) {
        this.decisionOption = decisionOption;
    }

    public String getDecisionSummary() {
        return decisionSummary;
    }

    public void setDecisionSummary(String decisionSummary) {
        this.decisionSummary = decisionSummary;
    }

    public String getDecisionOptionsJson() {
        return decisionOptionsJson;
    }

    public void setDecisionOptionsJson(String decisionOptionsJson) {
        this.decisionOptionsJson = decisionOptionsJson;
    }

    public String getMinutesJson() {
        return minutesJson;
    }

    public void setMinutesJson(String minutesJson) {
        this.minutesJson = minutesJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
