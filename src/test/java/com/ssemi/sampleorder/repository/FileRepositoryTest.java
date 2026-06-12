package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.ProductionJobStatus;
import com.ssemi.sampleorder.domain.model.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void persistsSamplesAndReloadsFromDisk() {
        Sample sample = new Sample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        FileSampleRepository first = new FileSampleRepository(tempDir.resolve("samples.json"));

        first.save(sample);

        FileSampleRepository second = new FileSampleRepository(tempDir.resolve("samples.json"));
        assertEquals(List.of(sample), second.findAll());
        assertEquals(sample, second.findById("S-001").orElseThrow());
    }

    @Test
    void persistsOrdersWithTimestampsAndStatus() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 9, 0);
        Order order = Order.reserve("ORD-0001", "S-001", "AI Lab", 5, now)
                .toConfirmed(5, now.plusMinutes(1));
        FileOrderRepository repository = new FileOrderRepository(tempDir.resolve("orders.json"));

        repository.save(order);

        FileOrderRepository reloaded = new FileOrderRepository(tempDir.resolve("orders.json"));
        assertEquals(OrderStatus.CONFIRMED, reloaded.findById("ORD-0001").orElseThrow().status());
        assertEquals(5, reloaded.findById("ORD-0001").orElseThrow().allocatedQuantity());
    }

    @Test
    void persistsProductionJobsAndReloadsInInsertionOrder() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 9, 0);
        ProductionJob firstJob = ProductionJob.waiting("JOB-0001", "ORD-0001", "S-003", 10, 13, 0.8, now);
        ProductionJob secondJob = ProductionJob.waiting("JOB-0002", "ORD-0002", "S-003", 5, 7, 0.8, now.plusMinutes(1))
                .start(now.plusMinutes(2));
        FileProductionJobRepository repository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));

        repository.save(firstJob);
        repository.save(secondJob);

        FileProductionJobRepository reloaded = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        assertEquals(List.of("JOB-0001", "JOB-0002"), reloaded.findAll().stream().map(ProductionJob::id).toList());
        assertEquals(ProductionJobStatus.RUNNING, reloaded.findById("JOB-0002").orElseThrow().status());
    }

    @Test
    void missingAndBlankFilesLoadAsEmptyCollections() throws Exception {
        Path file = tempDir.resolve("samples.json");
        FileSampleRepository missingRepository = new FileSampleRepository(file);
        assertTrue(missingRepository.findAll().isEmpty());

        Files.writeString(file, "");
        FileSampleRepository blankRepository = new FileSampleRepository(file);
        assertTrue(blankRepository.findAll().isEmpty());
    }

    @Test
    void persistsSequenceNumbersAcrossRepositoryInstances() {
        FileSequenceRepository first = new FileSequenceRepository(tempDir.resolve("sequences.json"));

        assertEquals(1, first.nextOrderNumber());
        assertEquals(2, first.nextOrderNumber());
        assertEquals(1, first.nextProductionJobNumber());

        FileSequenceRepository second = new FileSequenceRepository(tempDir.resolve("sequences.json"));
        assertEquals(3, second.nextOrderNumber());
        assertEquals(2, second.nextProductionJobNumber());
    }
}
