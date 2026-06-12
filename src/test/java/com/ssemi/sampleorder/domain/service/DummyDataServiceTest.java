package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.FileOrderRepository;
import com.ssemi.sampleorder.repository.FileProductionJobRepository;
import com.ssemi.sampleorder.repository.FileSampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyDataServiceTest {
    @TempDir
    Path tempDir;

    private FileSampleRepository sampleRepository;
    private FileOrderRepository orderRepository;
    private FileProductionJobRepository productionJobRepository;
    private DummyDataService service;

    @BeforeEach
    void setUp() {
        sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        productionJobRepository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        service = new DummyDataService(sampleRepository, orderRepository, productionJobRepository);
    }

    @Test
    void generatesDummyDataWithoutDuplicates() {
        service.generateIfMissing();
        service.generateIfMissing();

        assertEquals(5, sampleRepository.findAll().size());
        assertEquals(4, orderRepository.findAll().size());
        assertEquals(1, productionJobRepository.findAll().size());
    }

    @Test
    void dummyDataJobsReferenceExistingOrdersAndSamples() {
        service.generateIfMissing();

        Set<String> orderIds = orderRepository.findAll().stream().map(Order::id).collect(Collectors.toSet());
        Set<String> sampleIds = sampleRepository.findAll().stream().map(Sample::id).collect(Collectors.toSet());

        for (ProductionJob job : productionJobRepository.findAll()) {
            assertTrue(orderIds.contains(job.orderId()),
                    "JOB " + job.id() + " references missing order: " + job.orderId());
            assertTrue(sampleIds.contains(job.sampleId()),
                    "JOB " + job.id() + " references missing sample: " + job.sampleId());
        }
    }
}
