package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.repository.FileOrderRepository;
import com.ssemi.sampleorder.repository.FileProductionJobRepository;
import com.ssemi.sampleorder.repository.FileSampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DummyDataServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void generatesDummyDataWithoutDuplicates() {
        FileSampleRepository sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        FileOrderRepository orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        FileProductionJobRepository productionJobRepository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        DummyDataService service = new DummyDataService(sampleRepository, orderRepository, productionJobRepository);

        service.generateIfMissing();
        service.generateIfMissing();

        assertEquals(5, sampleRepository.findAll().size());
        assertEquals(4, orderRepository.findAll().size());
        assertEquals(2, productionJobRepository.findAll().size());
    }
}
