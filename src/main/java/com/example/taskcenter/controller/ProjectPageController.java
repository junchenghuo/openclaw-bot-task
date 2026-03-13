package com.example.taskcenter.controller;

import com.example.taskcenter.model.TaskStatus;
import com.example.taskcenter.service.ProjectService;
import com.example.taskcenter.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProjectPageController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectPageController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.listProjects());
        return "projects";
    }

    @GetMapping("/projects/{id}")
    public String projectDetail(@PathVariable("id") Long id,
                                @RequestParam(value = "status", required = false) TaskStatus status,
                                Model model) {
        model.addAttribute("project", projectService.getProject(id));
        model.addAttribute("tasks", taskService.listTasks(id, status));
        model.addAttribute("status", status);
        model.addAttribute("allStatuses", TaskStatus.values());
        return "project-detail";
    }

    @GetMapping("/projects/{id}/tasks-fragment")
    public String projectTaskFragment(@PathVariable("id") Long id,
                                      @RequestParam(value = "status", required = false) TaskStatus status,
                                      Model model) {
        model.addAttribute("tasks", taskService.listTasks(id, status));
        return "fragments/task-table :: taskTable";
    }
}
