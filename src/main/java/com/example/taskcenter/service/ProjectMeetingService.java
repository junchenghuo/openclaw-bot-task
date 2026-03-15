package com.example.taskcenter.service;

import com.example.taskcenter.dto.request.CastMeetingVoteRequest;
import com.example.taskcenter.dto.request.CloseMeetingRequest;
import com.example.taskcenter.dto.request.CreateProjectMeetingRequest;
import com.example.taskcenter.dto.response.MeetingParticipantResponse;
import com.example.taskcenter.dto.response.MeetingVoteResponse;
import com.example.taskcenter.dto.response.ProjectMeetingResponse;
import com.example.taskcenter.entity.MeetingParticipant;
import com.example.taskcenter.entity.MeetingVote;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.entity.ProjectMeeting;
import com.example.taskcenter.entity.Task;
import com.example.taskcenter.model.MeetingStatus;
import com.example.taskcenter.repository.MeetingParticipantRepository;
import com.example.taskcenter.repository.MeetingVoteRepository;
import com.example.taskcenter.repository.ProjectMeetingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProjectMeetingService {

    private static final DateTimeFormatter CODE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ProjectMeetingRepository projectMeetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingVoteRepository meetingVoteRepository;
    private final ObjectMapper objectMapper;

    public ProjectMeetingService(ProjectService projectService,
                                 TaskService taskService,
                                 ProjectMeetingRepository projectMeetingRepository,
                                 MeetingParticipantRepository meetingParticipantRepository,
                                 MeetingVoteRepository meetingVoteRepository,
                                 ObjectMapper objectMapper) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.projectMeetingRepository = projectMeetingRepository;
        this.meetingParticipantRepository = meetingParticipantRepository;
        this.meetingVoteRepository = meetingVoteRepository;
        this.objectMapper = objectMapper;
    }

    public List<ProjectMeetingResponse> listMeetings(Long projectId) {
        projectService.getProject(projectId);
        return projectMeetingRepository.findByProject_IdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProjectMeetingResponse> listMeetings(Long projectId, MeetingStatus status) {
        List<ProjectMeeting> meetings;
        if (projectId != null && status != null) {
            projectService.getProject(projectId);
            meetings = projectMeetingRepository.findByProject_IdAndStatusOrderByCreatedAtDesc(projectId, status);
        } else if (projectId != null) {
            projectService.getProject(projectId);
            meetings = projectMeetingRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
        } else if (status != null) {
            meetings = projectMeetingRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            meetings = projectMeetingRepository.findAllByOrderByCreatedAtDesc();
        }

        return meetings.stream().map(this::toResponse).toList();
    }

    public ProjectMeetingResponse getMeeting(Long projectId, Long meetingId) {
        return toResponse(getMeetingInProject(projectId, meetingId));
    }

    @Transactional
    public ProjectMeetingResponse createMeeting(Long projectId, CreateProjectMeetingRequest request) {
        Project project = projectService.getProject(projectId);
        validateDecisionOptions(request.getDecisionOptions());

        ProjectMeeting meeting = new ProjectMeeting();
        meeting.setMeetingCode(generateMeetingCode());
        meeting.setProject(project);
        meeting.setTopic(request.getTopic().trim());
        meeting.setProblemStatement(request.getProblemStatement());
        meeting.setOrganizerName(request.getOrganizerName().trim());
        meeting.setStatus(MeetingStatus.VOTING);
        meeting.setScheduledAt(request.getScheduledAt());
        meeting.setDecisionOptionsJson(toJsonString(request.getDecisionOptions()));

        if (request.getRelatedTaskId() != null) {
            Task relatedTask = taskService.getTask(request.getRelatedTaskId());
            if (!relatedTask.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("relatedTaskId 不属于当前项目");
            }
            meeting.setRelatedTask(relatedTask);
        }

        ProjectMeeting saved = projectMeetingRepository.save(meeting);

        List<MeetingParticipant> participants = buildParticipants(saved, request);
        if (!participants.isEmpty()) {
            meetingParticipantRepository.saveAll(participants);
        }

        return toResponse(saved);
    }

    @Transactional
    public ProjectMeetingResponse castVote(Long projectId, Long meetingId, CastMeetingVoteRequest request) {
        ProjectMeeting meeting = getMeetingInProject(projectId, meetingId);
        if (meeting.getStatus() != MeetingStatus.VOTING) {
            throw new IllegalStateException("会议不在投票阶段，无法投票");
        }

        List<String> options = parseDecisionOptions(meeting.getDecisionOptionsJson());
        if (!options.contains(request.getOptionKey().trim())) {
            throw new IllegalArgumentException("optionKey 不在本次会议候选方案中");
        }

        String voterName = request.getVoterName().trim();
        MeetingVote vote = meetingVoteRepository.findByMeeting_IdAndVoterName(meetingId, voterName)
                .orElseGet(MeetingVote::new);
        vote.setMeeting(meeting);
        vote.setVoterName(voterName);
        vote.setVoterRole(request.getVoterRole());
        vote.setVoterMention(request.getVoterMention());
        vote.setOptionKey(request.getOptionKey().trim());
        vote.setReason(request.getReason());
        meetingVoteRepository.save(vote);

        return toResponse(meeting);
    }

    @Transactional
    public ProjectMeetingResponse closeMeeting(Long projectId, Long meetingId, CloseMeetingRequest request) {
        ProjectMeeting meeting = getMeetingInProject(projectId, meetingId);
        if (meeting.getStatus() != MeetingStatus.VOTING) {
            throw new IllegalStateException("会议不在投票阶段，无法结束");
        }

        List<MeetingVote> votes = meetingVoteRepository.findByMeeting_IdOrderByCreatedAtAsc(meetingId);
        if (votes.isEmpty()) {
            throw new IllegalStateException("会议尚无有效投票，无法决策");
        }

        List<String> options = parseDecisionOptions(meeting.getDecisionOptionsJson());
        String decisionOption = resolveDecisionOption(request, votes, options);

        meeting.setStatus(MeetingStatus.DECIDED);
        meeting.setDecisionOption(decisionOption);
        meeting.setDecisionSummary(request.getDecisionSummary().trim());
        meeting.setMinutesJson(buildMinutesJson(meeting, request.getOperatorName().trim(), votes, decisionOption));
        ProjectMeeting saved = projectMeetingRepository.save(meeting);

        return toResponse(saved);
    }

    private ProjectMeeting getMeetingInProject(Long projectId, Long meetingId) {
        ProjectMeeting meeting = projectMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        "Meeting not found: " + meetingId,
                        HttpStatus.NOT_FOUND
                ));

        if (!meeting.getProject().getId().equals(projectId)) {
            throw new BusinessException(
                    ErrorCodes.NOT_FOUND,
                    "Meeting not found in project: " + projectId,
                    HttpStatus.NOT_FOUND
            );
        }
        return meeting;
    }

    private List<MeetingParticipant> buildParticipants(ProjectMeeting meeting, CreateProjectMeetingRequest request) {
        List<CreateProjectMeetingRequest.Participant> items = request.getParticipants() == null
                ? List.of()
                : request.getParticipants();

        List<MeetingParticipant> participants = new ArrayList<>();
        for (CreateProjectMeetingRequest.Participant item : items) {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeeting(meeting);
            participant.setMemberName(item.getName().trim());
            participant.setMemberRole(item.getRole());
            participant.setMemberMention(item.getMention());
            participant.setResponsibility(item.getResponsibility());
            participants.add(participant);
        }
        return participants;
    }

    private void validateDecisionOptions(List<String> decisionOptions) {
        if (decisionOptions == null || decisionOptions.isEmpty()) {
            throw new IllegalArgumentException("decisionOptions 不能为空");
        }
        for (String option : decisionOptions) {
            if (option == null || option.trim().isEmpty()) {
                throw new IllegalArgumentException("decisionOptions 中存在空值");
            }
        }
    }

    private ProjectMeetingResponse toResponse(ProjectMeeting meeting) {
        List<MeetingParticipantResponse> participants = meetingParticipantRepository
                .findByMeeting_IdOrderByIdAsc(meeting.getId())
                .stream()
                .map(MeetingParticipantResponse::from)
                .toList();
        List<MeetingVoteResponse> votes = meetingVoteRepository
                .findByMeeting_IdOrderByCreatedAtAsc(meeting.getId())
                .stream()
                .map(MeetingVoteResponse::from)
                .toList();
        List<String> decisionOptionsList = parseDecisionOptions(meeting.getDecisionOptionsJson());
        Map<String, Long> voteSummary = buildVoteSummary(votes, decisionOptionsList);
        return ProjectMeetingResponse.from(meeting, participants, votes, decisionOptionsList, voteSummary);
    }

    private Map<String, Long> buildVoteSummary(List<MeetingVoteResponse> votes,
                                               List<String> decisionOptionsList) {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (String option : decisionOptionsList) {
            summary.put(option, 0L);
        }
        for (MeetingVoteResponse vote : votes) {
            String option = vote.optionKey();
            summary.put(option, summary.getOrDefault(option, 0L) + 1L);
        }
        return summary;
    }

    private List<String> parseDecisionOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("会议候选方案格式异常");
        }
    }

    private String toJsonString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON 字段格式错误");
        }
    }

    private String resolveDecisionOption(CloseMeetingRequest request,
                                         List<MeetingVote> votes,
                                         List<String> options) {
        String specified = request.getDecisionOption();
        if (specified != null && !specified.trim().isEmpty()) {
            String candidate = specified.trim();
            if (!options.isEmpty() && !options.contains(candidate)) {
                throw new IllegalArgumentException("decisionOption 不在会议候选方案中");
            }
            return candidate;
        }

        Map<String, Long> count = new LinkedHashMap<>();
        for (MeetingVote vote : votes) {
            count.put(vote.getOptionKey(), count.getOrDefault(vote.getOptionKey(), 0L) + 1L);
        }

        String bestOption = null;
        long bestVotes = -1L;
        for (Map.Entry<String, Long> entry : count.entrySet()) {
            if (entry.getValue() > bestVotes) {
                bestVotes = entry.getValue();
                bestOption = entry.getKey();
            }
        }
        return bestOption;
    }

    private String buildMinutesJson(ProjectMeeting meeting,
                                    String operatorName,
                                    List<MeetingVote> votes,
                                    String decisionOption) {
        Map<String, Long> count = new LinkedHashMap<>();
        for (MeetingVote vote : votes) {
            count.put(vote.getOptionKey(), count.getOrDefault(vote.getOptionKey(), 0L) + 1L);
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("meetingCode", meeting.getMeetingCode());
        root.put("topic", meeting.getTopic());
        root.put("closedBy", operatorName);
        root.put("closedAt", LocalDateTime.now().toString());
        root.put("decisionOption", decisionOption);
        root.put("decisionSummary", meeting.getDecisionSummary());
        root.set("voteCount", objectMapper.valueToTree(count));
        root.set("votes", objectMapper.valueToTree(votes.stream().map(vote -> Map.of(
                "voter", vote.getVoterName(),
                "option", vote.getOptionKey(),
                "reason", vote.getReason() == null ? "" : vote.getReason()
        )).toList()));

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("会议纪要生成失败");
        }
    }

    private String generateMeetingCode() {
        for (int i = 0; i < 5; i++) {
            String code = "MEET-" + LocalDateTime.now().format(CODE_TIME)
                    + "-" + String.format(Locale.ROOT, "%04d", ThreadLocalRandom.current().nextInt(0, 10000));
            if (!projectMeetingRepository.existsByMeetingCode(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Could not generate unique meeting code", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
