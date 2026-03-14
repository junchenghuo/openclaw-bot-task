package com.example.taskcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_vote")
public class MeetingVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meeting_id", nullable = false)
    private ProjectMeeting meeting;

    @Column(name = "voter_name", nullable = false, length = 100)
    private String voterName;

    @Column(name = "voter_role", length = 64)
    private String voterRole;

    @Column(name = "voter_mention", length = 64)
    private String voterMention;

    @Column(name = "option_key", nullable = false, length = 200)
    private String optionKey;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectMeeting getMeeting() {
        return meeting;
    }

    public void setMeeting(ProjectMeeting meeting) {
        this.meeting = meeting;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
