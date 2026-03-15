package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.ProjectMeeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProjectMeetingResponse(
        Long id,
        String meetingCode,
        Long projectId,
        String projectName,
        Long relatedTaskId,
        String topic,
        String problemStatement,
        String organizerName,
        String status,
        LocalDateTime scheduledAt,
        String decisionOption,
        String decisionSummary,
        String decisionOptions,
        List<String> decisionOptionsList,
        Map<String, Long> voteSummary,
        String minutes,
        List<MeetingParticipantResponse> participants,
        List<MeetingVoteResponse> votes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectMeetingResponse from(ProjectMeeting meeting,
                                              List<MeetingParticipantResponse> participants,
                                              List<MeetingVoteResponse> votes,
                                              List<String> decisionOptionsList,
                                              Map<String, Long> voteSummary) {
        return new ProjectMeetingResponse(
                meeting.getId(),
                meeting.getMeetingCode(),
                meeting.getProject().getId(),
                meeting.getProject().getProjectName(),
                meeting.getRelatedTask() == null ? null : meeting.getRelatedTask().getId(),
                meeting.getTopic(),
                meeting.getProblemStatement(),
                meeting.getOrganizerName(),
                meeting.getStatus().name(),
                meeting.getScheduledAt(),
                meeting.getDecisionOption(),
                meeting.getDecisionSummary(),
                meeting.getDecisionOptionsJson(),
                decisionOptionsList,
                voteSummary,
                meeting.getMinutesJson(),
                participants,
                votes,
                meeting.getCreatedAt(),
                meeting.getUpdatedAt()
        );
    }
}
