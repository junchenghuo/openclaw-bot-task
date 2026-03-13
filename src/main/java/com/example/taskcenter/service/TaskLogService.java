package com.example.taskcenter.service;

import com.example.taskcenter.entity.Task;
import com.example.taskcenter.entity.TaskLog;
import com.example.taskcenter.repository.TaskLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskLogService {

    private final TaskLogRepository taskLogRepository;

    public TaskLogService(TaskLogRepository taskLogRepository) {
        this.taskLogRepository = taskLogRepository;
    }

    public void createLog(Task task, String logType, String message, String payloadJson) {
        TaskLog taskLog = new TaskLog();
        taskLog.setTask(task);
        taskLog.setLogType(logType);
        taskLog.setMessage(message);
        taskLog.setPayloadJson(payloadJson);
        taskLogRepository.save(taskLog);
    }

    public List<TaskLog> listTaskLogs(Long taskId) {
        return taskLogRepository.findByTask_IdOrderByCreatedAtDesc(taskId);
    }
}
