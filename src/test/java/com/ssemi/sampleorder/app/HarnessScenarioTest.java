package com.ssemi.sampleorder.app;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.service.DummyDataService;
import com.ssemi.sampleorder.domain.service.MonitoringService;
import com.ssemi.sampleorder.domain.service.OrderService;
import com.ssemi.sampleorder.domain.service.ProductionService;
import com.ssemi.sampleorder.domain.service.ReleaseService;
import com.ssemi.sampleorder.repository.FileOrderRepository;
import com.ssemi.sampleorder.repository.FileProductionJobRepository;
import com.ssemi.sampleorder.repository.FileSampleRepository;
import com.ssemi.sampleorder.repository.FileSequenceRepository;
import com.ssemi.sampleorder.util.MutableTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarnessScenarioTest {
    @TempDir
    Path tempDir;

    @Test
    void runsShortageProductionReleaseMonitoringScenario() {
        FileSampleRepository sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        FileOrderRepository orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        FileProductionJobRepository productionJobRepository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        FileSequenceRepository sequenceRepository = new FileSequenceRepository(tempDir.resolve("sequences.json"));
        MutableTimeProvider timeProvider = new MutableTimeProvider(LocalDateTime.of(2026, 6, 12, 9, 0));
        new DummyDataService(sampleRepository, orderRepository, productionJobRepository).generateIfMissing();
        ProductionService productionService = new ProductionService(sampleRepository, orderRepository, productionJobRepository, sequenceRepository, timeProvider);
        OrderService orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider, productionService);
        ReleaseService releaseService = new ReleaseService(sampleRepository, orderRepository, timeProvider);
        MonitoringService monitoringService = new MonitoringService(sampleRepository, orderRepository, productionJobRepository);

        Order order = orderService.reserveOrder("S-003", "Scenario Lab", 40);
        orderService.approveOrder(order.id());
        timeProvider.advanceMinutes(11);
        productionService.synchronizeProductionLine();
        Order confirmed = orderRepository.findById(order.id()).orElseThrow();
        Order released = releaseService.releaseOrder(order.id());

        assertEquals(OrderStatus.CONFIRMED, confirmed.status());
        assertEquals(OrderStatus.RELEASE, released.status());
        assertEquals(2, monitoringService.createSnapshot().activeOrderCounts().get(OrderStatus.RELEASE));
    }
}
