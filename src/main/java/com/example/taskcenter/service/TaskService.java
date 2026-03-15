package com.example.taskcenter.service;

import com.example.taskcenter.dto.request.BlockTaskRequest;
import com.example.taskcenter.dto.request.CancelTaskRequest;
import com.example.taskcenter.dto.request.CompleteTaskRequest;
import com.example.taskcenter.dto.request.CreateTaskRequest;
import com.example.taskcenter.dto.request.FailTaskRequest;
import com.example.taskcenter.dto.request.StartTaskRequest;
import com.example.taskcenter.dto.request.UpdateTaskRequest;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.entity.Task;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.example.taskcenter.model.TaskPriority;
import com.example.taskcenter.model.TaskStatus;
import com.example.taskcenter.repository.TaskRepository;
import com.example.taskcenter.support.SnowflakeIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskStatusMachine taskStatusMachine;
    private final TaskLogService taskLogService;
    private final TaskEventService taskEventService;
    private final ObjectMapper objectMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public TaskService(TaskRepository taskRepository,
                       ProjectService projectService,
                       TaskStatusMachine taskStatusMachine,
                       TaskLogService taskLogService,
                       TaskEventService taskEventService,
                       ObjectMapper objectMapper,
                       SnowflakeIdGenerator snowflakeIdGenerator) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.taskStatusMachine = taskStatusMachine;
        this.taskLogService = taskLogService;
        this.taskEventService = taskEventService;
        this.objectMapper = objectMapper;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    public List<Task> listTasks(Long projectId, TaskStatus status) {
        if (projectId != null && status != null) {
            return taskRepository.findByProject_IdAndStatusOrderByCreatedAtDesc(projectId, status);
        }
        if (projectId != null) {
            return taskRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
        }
        if (status != null) {
            return taskRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        "Task not found: " + taskId,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = getTask(taskId);
        taskStatusMachine.assertTaskEditable(task.getStatus());
        boolean changed = false;

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new IllegalArgumentException("title 不能为空字符串");
            }
            task.setTitle(request.getTitle());
            changed = true;
        }
        if (request.getTaskType() != null) {
            if (request.getTaskType().isBlank()) {
                throw new IllegalArgumentException("taskType 不能为空字符串");
            }
            task.setTaskType(normalizeTaskType(request.getTaskType()));
            changed = true;
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
            changed = true;
        }
        if (request.getDetail() != null) {
            task.setDetail(request.getDetail());
            changed = true;
        }
        if (request.getOwnerName() != null) {
            task.setOwnerName(request.getOwnerName());
            changed = true;
        }
        if (request.getPlannedFinishAt() != null) {
            task.setPlannedFinishAt(request.getPlannedFinishAt());
            changed = true;
        }
        if (request.getInput() != null) {
            task.setInputJson(toJsonString(request.getInput()));
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("未提供可更新字段");
        }

        Task saved = taskRepository.save(task);
        taskEventService.createEvent(saved, "更新", task.getStatus().name(), task.getStatus().name(),
                request.getOperatorName(), "任务信息已更新");
        taskLogService.createLog(saved, "UPDATE", "任务基础信息已更新", null);
        return saved;
    }

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        Project project = projectService.getProject(request.getProjectId());
        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = getTask(request.getParentTaskId());
        }

        Task task = new Task();
        task.setTaskCode(generateTaskCode());
        task.setProject(project);
        task.setParentTask(parentTask);
        task.setTitle(request.getTitle());
        task.setTaskType(normalizeTaskType(request.getTaskType()));
        task.setStatus(TaskStatus.待处理);
        task.setPriority(request.getPriority() == null ? TaskPriority.中 : request.getPriority());
        task.setDetail(request.getDetail());
        task.setInitiator(request.getInitiator());
        task.setOwnerName(request.getOwnerName());
        task.setPlannedFinishAt(request.getPlannedFinishAt());
        task.setInputJson(toJsonString(request.getInput()));
        Task saved = taskRepository.save(task);

        taskEventService.createEvent(saved, "创建", null, TaskStatus.待处理.name(),
                request.getInitiator(), "任务已创建");
        taskLogService.createLog(saved, "SYSTEM", "任务已创建", saved.getInputJson());
        return saved;
    }

    @Transactional
    public Task startTask(Long taskId, StartTaskRequest request) {
        return transitionStatus(taskId, TaskStatus.进行中, request.getOperatorName(), null, null, null);
    }

    @Transactional
    public Task blockTask(Long taskId, BlockTaskRequest request) {
        return transitionStatus(taskId, TaskStatus.阻塞, request.getOperatorName(),
                request.getBlockReason(), request.getBlockerContact(), null);
    }

    @Transactional
    public Task completeTask(Long taskId, CompleteTaskRequest request) {
        return transitionStatus(taskId, TaskStatus.已完成, request.getOperatorName(),
                "任务完成", null, toJsonString(request.getOutput()));
    }

    @Transactional
    public Task failTask(Long taskId, FailTaskRequest request) {
        return transitionStatus(taskId, TaskStatus.失败, request.getOperatorName(), request.getReason(), null, null);
    }

    @Transactional
    public Task cancelTask(Long taskId, CancelTaskRequest request) {
        return transitionStatus(taskId, TaskStatus.已取消, request.getOperatorName(), request.getReason(), null, null);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTask(taskId);
        taskRepository.delete(task);
    }

    private Task transitionStatus(Long taskId,
                                  TaskStatus to,
                                  String operatorName,
                                  String note,
                                  String blockerContact,
                                  String outputJson) {
        Task task = getTask(taskId);
        TaskStatus from = task.getStatus();
        taskStatusMachine.assertTransitionAllowed(from, to);
        task.setStatus(to);

        if (to == TaskStatus.进行中 && task.getActualStartAt() == null) {
            task.setActualStartAt(LocalDateTime.now());
        }
        if (to == TaskStatus.阻塞) {
            task.setBlockerContact(blockerContact);
            task.setBlockReason(note);
        } else {
            task.setBlockerContact(null);
            task.setBlockReason(null);
        }
        if (to == TaskStatus.已完成) {
            task.setOutputJson(outputJson);
        }
        if (to == TaskStatus.已完成 || to == TaskStatus.失败 || to == TaskStatus.已取消) {
            task.setActualFinishAt(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);

        String eventNote = (note == null || note.isBlank()) ? "状态变更" : note;
        taskEventService.createEvent(saved, "状态变更", from.name(), to.name(), operatorName, eventNote);
        taskLogService.createLog(saved, "STATUS", "任务状态由 " + from + " 变更为 " + to, null);

        return saved;
    }

    private String generateTaskCode() {
        for (int i = 0; i < 3; i++) {
            String code = "T" + snowflakeIdGenerator.nextId();
            if (!taskRepository.existsByTaskCode(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Could not generate unique task code", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String toJsonString(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCodes.INVALID_ARGUMENT, "JSON 字段格式错误", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeTaskType(String taskType) {
        if (taskType == null) {
            return null;
        }
        String value = taskType.trim();
        return switch (value) {
            case "GENERAL" -> "通用";
            case "PROJECT" -> "项目";
            case "DOCUMENT" -> "文档";
            case "DEVELOPMENT" -> "开发";
            case "TEST" -> "测试";
            case "RESEARCH" -> "调研";
            case "OPERATION" -> "运维";
            case "BUGFIX" -> "缺陷修复";
            default -> value;
        };
    }
}
