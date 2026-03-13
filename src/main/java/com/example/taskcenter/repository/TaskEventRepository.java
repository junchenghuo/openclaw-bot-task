package com.example.taskcenter.repository;

import com.example.taskcenter.entity.TaskEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskEventRepository extends JpaRepository<TaskEvent, Long> {
    List<TaskEvent> findByTask_IdOrderByCreatedAtDesc(Long taskId);
}
