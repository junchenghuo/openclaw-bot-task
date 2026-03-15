package com.example.taskcenter.repository;

import com.example.taskcenter.entity.OutboxEvent;
import com.example.taskcenter.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<OutboxEvent> findTop50ByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
            List<OutboxStatus> statuses,
            LocalDateTime now
    );

    List<OutboxEvent> findByOrderByIdDesc(Pageable pageable);
}
