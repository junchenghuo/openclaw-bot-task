package com.example.taskcenter.controller;

import com.example.taskcenter.dto.response.ProjectMeetingResponse;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.service.ProjectMeetingService;
import com.example.taskcenter.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MeetingPageController {

    private final ProjectService projectService;
    private final ProjectMeetingService projectMeetingService;

    public MeetingPageController(ProjectService projectService, ProjectMeetingService projectMeetingService) {
        this.projectService = projectService;
        this.projectMeetingService = projectMeetingService;
    }

    @GetMapping("/meetings")
    public String meetings(@RequestParam(value = "projectId", required = false) Long projectId, Model model) {
        List<Project> projects = projectService.listProjects();
        Long selectedProjectId = projectId;
        if (selectedProjectId == null && !projects.isEmpty()) {
            selectedProjectId = projects.get(0).getId();
        }

        Project selectedProject = null;
        List<ProjectMeetingResponse> meetings = List.of();
        if (selectedProjectId != null) {
            selectedProject = projectService.getProject(selectedProjectId);
            meetings = projectMeetingService.listMeetings(selectedProjectId);
        }

        model.addAttribute("projects", projects);
        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("selectedProjectId", selectedProjectId);
        model.addAttribute("meetings", meetings);
        return "meetings";
    }
}
