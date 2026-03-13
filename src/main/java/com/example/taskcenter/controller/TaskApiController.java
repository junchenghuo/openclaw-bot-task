package com.example.taskcenter.controller;

import com.example.taskcenter.dto.request.BlockTaskRequest;
import com.example.taskcenter.dto.request.CancelTaskRequest;
import com.example.taskcenter.dto.request.CompleteTaskRequest;
import com.example.taskcenter.dto.request.CreateTaskRequest;
import com.example.taskcenter.dto.request.FailTaskRequest;
import com.example.taskcenter.dto.request.StartTaskRequest;
import com.example.taskcenter.dto.request.UpdateTaskRequest;
import com.example.taskcenter.dto.response.ApiResponse;
import com.example.taskcenter.dto.response.TaskEventResponse;
import com.example.taskcenter.dto.response.TaskLogResponse;
import com.example.taskcenter.dto.response.TaskResponse;
import com.example.taskcenter.model.TaskStatus;
import com.example.taskcenter.service.TaskEventService;
import com.example.taskcenter.service.TaskLogService;
import com.example.taskcenter.service.TaskService;
import com.example.taskcenter.support.RequestIdSupport;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskService taskService;
    private final TaskLogService taskLogService;
    private final TaskEventService taskEventService;

    public TaskApiController(TaskService taskService,
                             TaskLogService taskLogService,
                             TaskEventService taskEventService) {
        this.taskService = taskService;
        this.taskLogService = taskLogService;
        this.taskEventService = taskEventService;
    }

    @Operation(summary = "查询任务列表", description = "按 projectId/status 可选过滤")
    @GetMapping
    public ApiResponse<List<TaskResponse>> listTasks(@RequestParam(value = "projectId", required = false) Long projectId,
                                                     @RequestParam(value = "status", required = false) TaskStatus status,
                                                     HttpServletRequest request) {
        List<TaskResponse> data = taskService.listTasks(projectId, status).stream().map(TaskResponse::from).toList();
        return ApiResponse.success(data, RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "查询任务详情", description = "通过任务ID查询")
    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable("id") Long id, HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.getTask(id)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "创建任务", description = "创建一条 PENDING 状态任务")
    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest body, HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.createTask(body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "更新任务", description = "更新任务的基础字段，终态任务不可编辑")
    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> updateTask(@PathVariable("id") Long id,
                                                @Valid @RequestBody UpdateTaskRequest body,
                                                HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.updateTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "开始任务", description = "PENDING/BLOCKED/FAILED -> RUNNING")
    @PostMapping("/{id}/start")
    public ApiResponse<TaskResponse> startTask(@PathVariable("id") Long id,
                                               @Valid @RequestBody StartTaskRequest body,
                                               HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.startTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "阻塞任务", description = "RUNNING -> BLOCKED")
    @PostMapping("/{id}/block")
    public ApiResponse<TaskResponse> blockTask(@PathVariable("id") Long id,
                                               @Valid @RequestBody BlockTaskRequest body,
                                               HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.blockTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "完成任务", description = "RUNNING -> COMPLETED")
    @PostMapping("/{id}/complete")
    public ApiResponse<TaskResponse> completeTask(@PathVariable("id") Long id,
                                                  @Valid @RequestBody CompleteTaskRequest body,
                                                  HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.completeTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "失败任务", description = "RUNNING/BLOCKED -> FAILED")
    @PostMapping("/{id}/fail")
    public ApiResponse<TaskResponse> failTask(@PathVariable("id") Long id,
                                              @Valid @RequestBody FailTaskRequest body,
                                              HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.failTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "取消任务", description = "PENDING/RUNNING/BLOCKED -> CANCELLED")
    @PostMapping("/{id}/cancel")
    public ApiResponse<TaskResponse> cancelTask(@PathVariable("id") Long id,
                                                @Valid @RequestBody CancelTaskRequest body,
                                                HttpServletRequest request) {
        return ApiResponse.success(TaskResponse.from(taskService.cancelTask(id, body)), RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "查询任务日志", description = "按任务ID查询日志")
    @GetMapping("/{id}/logs")
    public ApiResponse<List<TaskLogResponse>> getTaskLogs(@PathVariable("id") Long id, HttpServletRequest request) {
        List<TaskLogResponse> data = taskLogService.listTaskLogs(id).stream().map(TaskLogResponse::from).toList();
        return ApiResponse.success(data, RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "查询任务事件", description = "按任务ID查询事件")
    @GetMapping("/{id}/events")
    public ApiResponse<List<TaskEventResponse>> getTaskEvents(@PathVariable("id") Long id, HttpServletRequest request) {
        List<TaskEventResponse> data = taskEventService.listTaskEvents(id).stream().map(TaskEventResponse::from).toList();
        return ApiResponse.success(data, RequestIdSupport.getOrCreate(request));
    }
}
