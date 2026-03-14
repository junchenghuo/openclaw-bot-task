package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.MeetingParticipant;

public record MeetingParticipantResponse(
        Long id,
        String memberName,
        String memberRole,
        String memberMention,
        String responsibility
) {
    public static MeetingParticipantResponse from(MeetingParticipant participant) {
        return new MeetingParticipantResponse(
                participant.getId(),
                participant.getMemberName(),
                participant.getMemberRole(),
                participant.getMemberMention(),
                participant.getResponsibility()
        );
    }
}
