package com.ssemi.sampleorder.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductionJobTest {
    @Test
    void startsAndCompletesProductionJob() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 9, 0);
        ProductionJob job = ProductionJob.waiting("JOB-0001", "ORD-0001", "S-003", 10, 13, 0.8, now);

        ProductionJob running = job.start(now.plusMinutes(1));
        ProductionJob completed = running.complete(now.plusMinutes(12));

        assertEquals(ProductionJobStatus.RUNNING, running.status());
        assertEquals(now.plusMinutes(1), running.startedAt());
        assertEquals(now.plusMinutes(1).plusMinutes(11), running.expectedEndAt());
        assertEquals(ProductionJobStatus.COMPLETED, completed.status());
        assertEquals(now.plusMinutes(12), completed.completedAt());
    }
}
