package com.example.taskcenter.controller;

import com.example.taskcenter.dto.request.CastMeetingVoteRequest;
import com.example.taskcenter.dto.request.CloseMeetingRequest;
import com.example.taskcenter.dto.request.CreateProjectMeetingRequest;
import com.example.taskcenter.dto.response.ApiResponse;
import com.example.taskcenter.dto.response.ProjectMeetingResponse;
import com.example.taskcenter.service.ProjectMeetingService;
import com.example.taskcenter.support.RequestIdSupport;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/meetings")
public class ProjectMeetingApiController {

    private final ProjectMeetingService projectMeetingService;

    public ProjectMeetingApiController(ProjectMeetingService projectMeetingService) {
        this.projectMeetingService = projectMeetingService;
    }

    @Operation(summary = "查询项目会议列表", description = "按项目查询会议，含投票与纪要")
    @GetMapping
    public ApiResponse<List<ProjectMeetingResponse>> listMeetings(@PathVariable("projectId") Long projectId,
                                                                  HttpServletRequest request) {
        return ApiResponse.success(projectMeetingService.listMeetings(projectId), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "查询会议详情", description = "返回会议参与人、投票、决策纪要")
    @GetMapping("/{meetingId}")
    public ApiResponse<ProjectMeetingResponse> getMeeting(@PathVariable("projectId") Long projectId,
                                                          @PathVariable("meetingId") Long meetingId,
                                                          HttpServletRequest request) {
        return ApiResponse.success(projectMeetingService.getMeeting(projectId, meetingId), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "创建项目会议", description = "Leader 发起会议并邀请成员，进入投票阶段")
    @PostMapping
    public ApiResponse<ProjectMeetingResponse> createMeeting(@PathVariable("projectId") Long projectId,
                                                             @Valid @RequestBody CreateProjectMeetingRequest body,
                                                             HttpServletRequest request) {
        return ApiResponse.success(projectMeetingService.createMeeting(projectId, body), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "会议投票", description = "成员对会议候选方案投票")
    @PostMapping("/{meetingId}/votes")
    public ApiResponse<ProjectMeetingResponse> castVote(@PathVariable("projectId") Long projectId,
                                                        @PathVariable("meetingId") Long meetingId,
                                                        @Valid @RequestBody CastMeetingVoteRequest body,
                                                        HttpServletRequest request) {
        return ApiResponse.success(projectMeetingService.castVote(projectId, meetingId, body), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "关闭会议并形成决策", description = "按投票结果确认最终决策并写入会议纪要")
    @PostMapping("/{meetingId}/close")
    public ApiResponse<ProjectMeetingResponse> closeMeeting(@PathVariable("projectId") Long projectId,
                                                            @PathVariable("meetingId") Long meetingId,
                                                            @Valid @RequestBody CloseMeetingRequest body,
                                                            HttpServletRequest request) {
        return ApiResponse.success(projectMeetingService.closeMeeting(projectId, meetingId, body), RequestIdSupport.getOrCreate(request));
    }
}
