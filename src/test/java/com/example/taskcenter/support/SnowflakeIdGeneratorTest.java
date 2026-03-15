package com.example.taskcenter.support;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeIdGeneratorTest {

    @Test
    void nextIdShouldBeUniqueAndMonotonic() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

        Set<Long> seen = new HashSet<>();
        long prev = -1L;
        for (int i = 0; i < 10_000; i++) {
            long id = generator.nextId();
            assertTrue(id > 0, "id 必须大于 0");
            assertTrue(seen.add(id), "id 必须全局唯一");
            assertTrue(id > prev, "id 必须单调递增");
            prev = id;
        }
    }
}
