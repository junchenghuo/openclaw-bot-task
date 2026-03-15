package com.example.taskcenter.service;

import com.example.taskcenter.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskStatusMachineTest {

    private final TaskStatusMachine machine = new TaskStatusMachine();

    @Test
    void shouldAllowPendingToRunning() {
        assertDoesNotThrow(() -> machine.assertTransitionAllowed(TaskStatus.待处理, TaskStatus.进行中));
    }

    @Test
    void shouldAllowFailedToRunningForRetry() {
        assertDoesNotThrow(() -> machine.assertTransitionAllowed(TaskStatus.失败, TaskStatus.进行中));
    }

    @Test
    void shouldRejectPendingToCompleted() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTransitionAllowed(TaskStatus.待处理, TaskStatus.已完成));
    }

    @Test
    void shouldRejectCompletedToRunning() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTransitionAllowed(TaskStatus.已完成, TaskStatus.进行中));
    }

    @Test
    void shouldRejectEditWhenTaskIsCompleted() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTaskEditable(TaskStatus.已完成));
    }

    @Test
    void shouldAllowEditWhenTaskIsRunning() {
        assertDoesNotThrow(() -> machine.assertTaskEditable(TaskStatus.进行中));
    }
}
