package com.example.taskcenter.controller;

import com.example.taskcenter.dto.response.ApiResponse;
import com.example.taskcenter.dto.response.ProjectResponse;
import com.example.taskcenter.service.ProjectService;
import com.example.taskcenter.support.RequestIdSupport;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
