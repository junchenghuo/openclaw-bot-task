package com.example.taskcenter.controller;

import com.example.taskcenter.service.TaskEventService;
import com.example.taskcenter.service.TaskLogService;
import com.example.taskcenter.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TaskPageController {

    private final TaskService taskService;
    private final TaskLogService taskLogService;
    private final TaskEventService taskEventService;

    public TaskPageController(TaskService taskService,
                              TaskLogService taskLogService,
                              TaskEventService taskEventService) {
        this.taskService = taskService;
        this.taskLogService = taskLogService;
        this.taskEventService = taskEventService;
    }

    @GetMapping("/tasks/{id}")
    public String taskDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("task", taskService.getTask(id));
        model.addAttribute("logs", taskLogService.listTaskLogs(id));
        model.addAttribute("events", taskEventService.listTaskEvents(id));
        return "task-detail";
    }
}
