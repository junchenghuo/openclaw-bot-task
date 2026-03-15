package com.example.taskcenter.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 简化版雪花算法（41bit 时间戳 + 5bit 机房 + 5bit 机器 + 12bit 序列）。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00 UTC
    private static final long DATACENTER_BITS = 5L;
    private static final long WORKER_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    private final long workerId;
    private final long datacenterId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(
            @Value("${task.snowflake.worker-id:1}") long workerId,
            @Value("${task.snowflake.datacenter-id:1}") long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 超出范围: " + workerId);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException("datacenterId 超出范围: " + datacenterId);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long ts = currentTimeMillis();

        if (ts < lastTimestamp) {
            // 时钟回拨时，等待到上次时间戳，避免重复ID。
            ts = waitUntil(lastTimestamp);
        }

        if (ts == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                ts = waitUntil(lastTimestamp + 1);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = ts;

        return ((ts - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long waitUntil(long targetTimestamp) {
        long ts = currentTimeMillis();
        while (ts < targetTimestamp) {
            ts = currentTimeMillis();
        }
        return ts;
    }
}
