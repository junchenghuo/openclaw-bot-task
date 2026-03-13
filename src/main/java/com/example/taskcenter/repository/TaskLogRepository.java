package com.example.taskcenter.repository;

import com.example.taskcenter.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    List<TaskLog> findByTask_IdOrderByCreatedAtDesc(Long taskId);
}
