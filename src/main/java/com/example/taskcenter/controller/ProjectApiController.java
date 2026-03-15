package com.example.taskcenter.controller;

import com.example.taskcenter.dto.request.CreateProjectRequest;
import com.example.taskcenter.dto.request.BindProjectChannelRequest;
import com.example.taskcenter.dto.response.ApiResponse;
import com.example.taskcenter.dto.response.ProjectResponse;
import com.example.taskcenter.service.ProjectService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {

    private final ProjectService projectService;

    public ProjectApiController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "查询项目列表", description = "返回全部项目")
    @GetMapping
    public ApiResponse<List<ProjectResponse>> listProjects(HttpServletRequest request) {
        List<ProjectResponse> data = projectService.listProjects().stream().map(ProjectResponse::from).toList();
        return ApiResponse.success(data, RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "查询项目详情", description = "通过项目ID查询")
    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getProject(@PathVariable("id") Long id, HttpServletRequest request) {
        return ApiResponse.success(ProjectResponse.from(projectService.getProject(id)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "创建项目", description = "创建项目并初始化工作/记忆目录")
    @PostMapping
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest body,
                                                      HttpServletRequest request) {
        return ApiResponse.success(ProjectResponse.from(projectService.createProject(body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "删除项目", description = "删除项目及其下任务与会议")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable("id") Long id, HttpServletRequest request) {
        projectService.deleteProject(id);
        return ApiResponse.success(null, RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "绑定项目频道", description = "为项目绑定 Mattermost 频道ID/名称")
    @PutMapping("/{id}/channel")
    public ApiResponse<ProjectResponse> bindProjectChannel(@PathVariable("id") Long id,
                                                           @Valid @RequestBody BindProjectChannelRequest body,
                                                           HttpServletRequest request) {
        return ApiResponse.success(
                ProjectResponse.from(projectService.bindProjectChannel(id, body.getMattermostChannelId(), body.getMattermostChannelName())),
                RequestIdSupport.getOrCreate(request)
        );
    }
}
