package com.example.taskcenter.service;

import com.example.taskcenter.dto.response.DashboardResponse;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.model.MeetingStatus;
import com.example.taskcenter.model.TaskStatus;
import com.example.taskcenter.repository.ProjectMeetingRepository;
import com.example.taskcenter.repository.ProjectRepository;
import com.example.taskcenter.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMeetingRepository projectMeetingRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            ProjectMeetingRepository projectMeetingRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMeetingRepository = projectMeetingRepository;
    }

    public DashboardResponse buildDashboard() {
        long projectTotal = projectRepository.count();
        long taskTotal = taskRepository.count();
        long meetingTotal = projectMeetingRepository.count();
        long pendingTotal = taskRepository.countByStatus(TaskStatus.待处理);
        long runningTotal = taskRepository.countByStatus(TaskStatus.进行中);
        long completedTotal = taskRepository.countByStatus(TaskStatus.已完成);
        long failedTotal = taskRepository.countByStatus(TaskStatus.失败);
        long blockedTotal = taskRepository.countByStatus(TaskStatus.阻塞);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        long meetingTodayTotal = projectMeetingRepository.countByScheduledAtBetween(todayStart, tomorrowStart);
        long meetingVotingTotal = projectMeetingRepository.countByStatus(MeetingStatus.投票中);
        long meetingDecidedTotal = projectMeetingRepository.countByStatus(MeetingStatus.已决策);
        long todayCreatedTotal = taskRepository.countByCreatedAtBetween(todayStart, tomorrowStart);
        long todayCompletedTotal = taskRepository.countByStatusAndActualFinishAtBetween(
                TaskStatus.已完成, todayStart, tomorrowStart);

        Map<Long, String> projectNameMap = projectRepository.findAll().stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectName));

        List<DashboardResponse.NamedCount> projectTaskCounts = taskRepository.countByProject().stream()
                .map(it -> new DashboardResponse.NamedCount(
                        projectNameMap.getOrDefault(it.getProjectId(), "未命名项目"),
                        it.getTotal()
                ))
                .toList();

        List<DashboardResponse.NamedCount> statusTaskCounts = List.of(
                new DashboardResponse.NamedCount(TaskStatus.待处理.name(), pendingTotal),
                new DashboardResponse.NamedCount(TaskStatus.进行中.name(), runningTotal),
                new DashboardResponse.NamedCount(TaskStatus.阻塞.name(), blockedTotal),
                new DashboardResponse.NamedCount(TaskStatus.已完成.name(), completedTotal),
                new DashboardResponse.NamedCount(TaskStatus.失败.name(), failedTotal),
                new DashboardResponse.NamedCount(TaskStatus.已取消.name(), taskRepository.countByStatus(TaskStatus.已取消))
        );

        List<DashboardResponse.DailyCount> recentSevenDays = buildRecentSevenDays();

        return new DashboardResponse(
                projectTotal,
                taskTotal,
                meetingTotal,
                meetingTodayTotal,
                meetingVotingTotal,
                meetingDecidedTotal,
                pendingTotal,
                runningTotal,
                completedTotal,
                failedTotal,
                blockedTotal,
                todayCreatedTotal,
                todayCompletedTotal,
                projectTaskCounts,
                statusTaskCounts,
                recentSevenDays
        );
    }

    private List<DashboardResponse.DailyCount> buildRecentSevenDays() {
        LocalDate start = LocalDate.now().minusDays(6);
        Map<LocalDate, Long> dayCounter = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            dayCounter.put(start.plusDays(i), 0L);
        }

        taskRepository.countRecentSevenDays().forEach(row -> {
            LocalDate day = row.getDay().toLocalDate();
            if (dayCounter.containsKey(day)) {
                dayCounter.put(day, row.getTotal());
            }
        });

        List<DashboardResponse.DailyCount> result = new ArrayList<>();
        dayCounter.forEach((day, count) -> result.add(new DashboardResponse.DailyCount(day.format(DATE_FORMAT), count)));
        return result;
    }
}
