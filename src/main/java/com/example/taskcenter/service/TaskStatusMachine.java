package com.example.taskcenter.service;

import com.example.taskcenter.model.TaskStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TaskStatusMachine {

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TaskStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TaskStatus.PENDING, EnumSet.of(TaskStatus.RUNNING, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.RUNNING,
                EnumSet.of(TaskStatus.COMPLETED, TaskStatus.FAILED, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.BLOCKED,
                EnumSet.of(TaskStatus.RUNNING, TaskStatus.FAILED, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.FAILED, EnumSet.of(TaskStatus.RUNNING));
        ALLOWED_TRANSITIONS.put(TaskStatus.COMPLETED, EnumSet.noneOf(TaskStatus.class));
        ALLOWED_TRANSITIONS.put(TaskStatus.CANCELLED, EnumSet.noneOf(TaskStatus.class));
    }

    public void assertTransitionAllowed(TaskStatus from, TaskStatus to) {
        if (from == to) {
            return;
        }

        Set<TaskStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TaskStatus.class));
        if (!allowed.contains(to)) {
            throw new IllegalStateException("Task status transition is not allowed: " + from + " -> " + to);
        }
    }

    public void assertTaskEditable(TaskStatus status) {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Terminal task cannot be edited: " + status);
        }
    }
}
