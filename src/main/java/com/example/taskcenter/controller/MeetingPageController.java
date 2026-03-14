package com.example.taskcenter.controller;

import com.example.taskcenter.dto.response.ProjectMeetingResponse;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.model.MeetingStatus;
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
    public String meetings(@RequestParam(value = "projectId", required = false) Long projectId,
                           @RequestParam(value = "status", required = false) MeetingStatus status,
                           Model model) {
        List<Project> projects = projectService.listProjects();
        Project selectedProject = null;
        if (projectId != null) {
            selectedProject = projectService.getProject(projectId);
        }
        List<ProjectMeetingResponse> meetings = projectMeetingService.listMeetings(projectId, status);

        model.addAttribute("projects", projects);
        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("status", status);
        model.addAttribute("allMeetingStatuses", MeetingStatus.values());
        model.addAttribute("meetings", meetings);
        return "meetings";
    }
}
