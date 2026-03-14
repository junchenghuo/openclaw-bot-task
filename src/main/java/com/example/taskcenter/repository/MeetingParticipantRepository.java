package com.example.taskcenter.repository;

import com.example.taskcenter.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findByMeeting_IdOrderByIdAsc(Long meetingId);
}
