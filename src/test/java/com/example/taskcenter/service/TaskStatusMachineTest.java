package com.example.taskcenter.service;

import com.example.taskcenter.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskStatusMachineTest {

    private final TaskStatusMachine machine = new TaskStatusMachine();

    @Test
    void shouldAllowPendingToRunning() {
        assertDoesNotThrow(() -> machine.assertTransitionAllowed(TaskStatus.PENDING, TaskStatus.RUNNING));
    }

    @Test
    void shouldAllowFailedToRunningForRetry() {
        assertDoesNotThrow(() -> machine.assertTransitionAllowed(TaskStatus.FAILED, TaskStatus.RUNNING));
    }

    @Test
    void shouldRejectPendingToCompleted() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTransitionAllowed(TaskStatus.PENDING, TaskStatus.COMPLETED));
    }

    @Test
    void shouldRejectCompletedToRunning() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTransitionAllowed(TaskStatus.COMPLETED, TaskStatus.RUNNING));
    }

    @Test
    void shouldRejectEditWhenTaskIsCompleted() {
        assertThrows(IllegalStateException.class,
                () -> machine.assertTaskEditable(TaskStatus.COMPLETED));
    }

    @Test
    void shouldAllowEditWhenTaskIsRunning() {
        assertDoesNotThrow(() -> machine.assertTaskEditable(TaskStatus.RUNNING));
    }
}
