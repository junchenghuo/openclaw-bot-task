package com.example.taskcenter.repository;

import com.example.taskcenter.entity.Task;
import com.example.taskcenter.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    interface ProjectTaskCount {
        Long getProjectId();

        Long getTotal();
    }

    interface DailyTaskCount {
        Date getDay();

        Long getTotal();
    }

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByProject_IdOrderByCreatedAtDesc(Long projectId);

    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    List<Task> findByProject_IdAndStatusOrderByCreatedAtDesc(Long projectId, TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndActualFinishAtBetween(TaskStatus status, LocalDateTime start, LocalDateTime end);

    boolean existsByTaskCode(String taskCode);

    @Query("""
            select t.project.id as projectId, count(t.id) as total
            from Task t
            group by t.project.id
            """)
    List<ProjectTaskCount> countByProject();

    @Query(value = """
            select cast(created_at as date) as day, count(id) as total
            from task
            where created_at >= current_date - interval '6 day'
            group by cast(created_at as date)
            order by day
            """, nativeQuery = true)
    List<DailyTaskCount> countRecentSevenDays();
}
