package com.example.taskcenter.repository;

import com.example.taskcenter.entity.ProjectMeeting;
import com.example.taskcenter.model.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectMeetingRepository extends JpaRepository<ProjectMeeting, Long> {
    List<ProjectMeeting> findByProject_IdOrderByCreatedAtDesc(Long projectId);
    boolean existsByMeetingCode(String meetingCode);

    long countByStatus(MeetingStatus status);

    long countByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
