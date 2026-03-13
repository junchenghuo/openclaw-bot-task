package com.example.taskcenter.entity;

import com.example.taskcenter.model.TaskPriority;
import com.example.taskcenter.model.TaskStatus;
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
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 64)
    private String taskCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "task_type", nullable = false, length = 64)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length = 100)
    private String initiator;

    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(name = "blocker_contact", length = 200)
    private String blockerContact;

    @Column(name = "block_reason", length = 500)
    private String blockReason;

    @Column(name = "planned_finish_at")
    private LocalDateTime plannedFinishAt;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_finish_at")
    private LocalDateTime actualFinishAt;

    @Column(name = "input_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String inputJson;

    @Column(name = "output_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String outputJson;

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

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
    
        this.initiator = initiator;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public LocalDateTime getPlannedFinishAt() {
        return plannedFinishAt;
    }

    public void setPlannedFinishAt(LocalDateTime plannedFinishAt) {
        this.plannedFinishAt = plannedFinishAt;
    }

    public LocalDateTime getActualStartAt() {
        return actualStartAt;
    }

    public void setActualStartAt(LocalDateTime actualStartAt) {
        this.actualStartAt = actualStartAt;
    }

    public LocalDateTime getActualFinishAt() {
        return actualFinishAt;
    }

    public void setActualFinishAt(LocalDateTime actualFinishAt) {
        this.actualFinishAt = actualFinishAt;
    }

    public String getInputJson() {
        return inputJson;
    }

    public void setInputJson(String inputJson) {
        this.inputJson = inputJson;
    }

    public String getOutputJson() {
        return outputJson;
    }

    public void setOutputJson(String outputJson) {
        this.outputJson = outputJson;
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
