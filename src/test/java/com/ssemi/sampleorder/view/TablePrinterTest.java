package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.domain.model.ProductionJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TablePrinterTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void setUp() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void printProductionJobsIncludesTimingForRunningJob() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 12, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 12, 9, 11);
        ProductionJob job = ProductionJob.waiting("JOB-0001", "ORD-0001", "S-001", 10, 13, 0.8, start)
                .start(start);

        TablePrinter.printProductionJobs(List.of(job));

        String output = captured.toString();
        assertTrue(output.contains("10.4"), "총 생산시간(10.4)이 출력에 포함되어야 함");
        assertTrue(output.contains(start.toString()), "시작 시각이 출력에 포함되어야 함");
        assertTrue(output.contains(job.expectedEndAt().toString()), "완료예정 시각이 출력에 포함되어야 함");
    }

    @Test
    void printProductionJobsShowsDashForWaitingJob() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 9, 0);
        ProductionJob job = ProductionJob.waiting("JOB-0001", "ORD-0001", "S-001", 10, 13, 0.8, now);

        TablePrinter.printProductionJobs(List.of(job));

        String output = captured.toString();
        assertTrue(output.contains("-"), "WAITING 작업의 시작/완료예정은 - 로 표시되어야 함");
    }
}
