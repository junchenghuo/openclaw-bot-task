package com.example.taskcenter.dto.response;

import com.example.taskcenter.entity.MeetingVote;

import java.time.LocalDateTime;

public record MeetingVoteResponse(
        Long id,
        String voterName,
        String voterRole,
        String voterMention,
        String optionKey,
        String reason,
        LocalDateTime createdAt
) {
    public static MeetingVoteResponse from(MeetingVote vote) {
        return new MeetingVoteResponse(
                vote.getId(),
                vote.getVoterName(),
                vote.getVoterRole(),
                vote.getVoterMention(),
                vote.getOptionKey(),
                vote.getReason(),
                vote.getCreatedAt()
        );
    }
}
