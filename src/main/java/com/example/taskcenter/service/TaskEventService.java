package com.example.taskcenter.service;

import com.example.taskcenter.entity.Task;
import com.example.taskcenter.entity.TaskEvent;
import com.example.taskcenter.repository.TaskEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskEventService {

    private final TaskEventRepository taskEventRepository;

    public TaskEventService(TaskEventRepository taskEventRepository) {
        this.taskEventRepository = taskEventRepository;
    }

    public void createEvent(Task task,
                            String eventType,
                            String fromStatus,
                            String toStatus,
                            String operatorName,
                            String note) {
        TaskEvent event = new TaskEvent();
        event.setTask(task);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperatorName(operatorName);
        event.setNote(note);
        taskEventRepository.save(event);
    }

    public List<TaskEvent> listTaskEvents(Long taskId) {
        return taskEventRepository.findByTask_IdOrderByCreatedAtDesc(taskId);
    }
}
