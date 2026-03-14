package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.ProjectMeeting;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectMeetingResponse(
        Long id,
        String meetingCode,
        Long projectId,
        Long relatedTaskId,
        String topic,
        String problemStatement,
        String organizerName,
        String status,
        LocalDateTime scheduledAt,
        String decisionOption,
        String decisionSummary,
        String decisionOptions,
        String minutes,
        List<MeetingParticipantResponse> participants,
        List<MeetingVoteResponse> votes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectMeetingResponse from(ProjectMeeting meeting,
                                              List<MeetingParticipantResponse> participants,
                                              List<MeetingVoteResponse> votes) {
        return new ProjectMeetingResponse(
                meeting.getId(),
                meeting.getMeetingCode(),
                meeting.getProject().getId(),
                meeting.getRelatedTask() == null ? null : meeting.getRelatedTask().getId(),
                meeting.getTopic(),
                meeting.getProblemStatement(),
                meeting.getOrganizerName(),
                meeting.getStatus().name(),
                meeting.getScheduledAt(),
                meeting.getDecisionOption(),
                meeting.getDecisionSummary(),
                meeting.getDecisionOptionsJson(),
                meeting.getMinutesJson(),
                participants,
                votes,
                meeting.getCreatedAt(),
                meeting.getUpdatedAt()
        );
    }
}
