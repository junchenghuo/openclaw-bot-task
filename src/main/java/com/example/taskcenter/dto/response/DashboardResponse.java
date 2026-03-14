package com.example.taskcenter.dto.response;

import java.util.List;

public record DashboardResponse(
        long projectTotal,
        long taskTotal,
        long meetingTotal,
        long meetingTodayTotal,
        long meetingVotingTotal,
        long meetingDecidedTotal,
        long pendingTotal,
        long runningTotal,
        long completedTotal,
        long failedTotal,
        long blockedTotal,
        long todayCreatedTotal,
        long todayCompletedTotal,
        List<NamedCount> projectTaskCounts,
        List<NamedCount> statusTaskCounts,
        List<DailyCount> recentSevenDays
) {
    public record NamedCount(String name, long count) {
    }

    public record DailyCount(String day, long count) {
    }
}
