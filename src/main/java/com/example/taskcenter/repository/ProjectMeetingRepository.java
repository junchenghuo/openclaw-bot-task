package com.example.taskcenter.repository;

import com.example.taskcenter.entity.ProjectMeeting;
import com.example.taskcenter.model.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectMeetingRepository extends JpaRepository<ProjectMeeting, Long> {
    List<ProjectMeeting> findByProject_IdOrderByCreatedAtDesc(Long projectId);
    List<ProjectMeeting> findAllByOrderByCreatedAtDesc();
    List<ProjectMeeting> findByStatusOrderByCreatedAtDesc(MeetingStatus status);
    List<ProjectMeeting> findByProject_IdAndStatusOrderByCreatedAtDesc(Long projectId, MeetingStatus status);
    boolean existsByMeetingCode(String meetingCode);
    void deleteByProject_Id(Long projectId);

    long countByStatus(MeetingStatus status);

    long countByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
