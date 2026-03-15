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
        ALLOWED_TRANSITIONS.put(TaskStatus.待处理, EnumSet.of(TaskStatus.进行中, TaskStatus.已取消));
        ALLOWED_TRANSITIONS.put(TaskStatus.进行中,
                EnumSet.of(TaskStatus.已完成, TaskStatus.失败, TaskStatus.阻塞, TaskStatus.已取消));
        ALLOWED_TRANSITIONS.put(TaskStatus.阻塞,
                EnumSet.of(TaskStatus.进行中, TaskStatus.失败, TaskStatus.已取消));
        ALLOWED_TRANSITIONS.put(TaskStatus.失败, EnumSet.of(TaskStatus.进行中));
        ALLOWED_TRANSITIONS.put(TaskStatus.已完成, EnumSet.noneOf(TaskStatus.class));
        ALLOWED_TRANSITIONS.put(TaskStatus.已取消, EnumSet.noneOf(TaskStatus.class));
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
        if (status == TaskStatus.已完成 || status == TaskStatus.已取消) {
            throw new IllegalStateException("Terminal task cannot be edited: " + status);
        }
    }
}
