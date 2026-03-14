package com.example.taskcenter.repository;

import com.example.taskcenter.entity.MeetingVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingVoteRepository extends JpaRepository<MeetingVote, Long> {
    List<MeetingVote> findByMeeting_IdOrderByCreatedAtAsc(Long meetingId);
    Optional<MeetingVote> findByMeeting_IdAndVoterName(Long meetingId, String voterName);
}
