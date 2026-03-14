package com.example.taskcenter.controller;

import com.example.taskcenter.model.TaskStatus;
import com.example.taskcenter.service.ProjectService;
import com.example.taskcenter.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TaskCenterPageController {

    private final TaskService taskService;
    private final ProjectService projectService;

    public TaskCenterPageController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping("/task-center")
    public String taskCenter(@RequestParam(value = "projectId", required = false) Long projectId,
                             @RequestParam(value = "status", required = false) TaskStatus status,
                             Model model) {
        model.addAttribute("tasks", taskService.listTasks(projectId, status));
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("status", status);
        model.addAttribute("allStatuses", TaskStatus.values());
        model.addAttribute("projects", projectService.listProjects());
        return "task-center";
    }
}
