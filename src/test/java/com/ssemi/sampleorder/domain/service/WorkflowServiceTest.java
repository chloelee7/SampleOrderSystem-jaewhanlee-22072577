package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.ProductionJobStatus;
import com.ssemi.sampleorder.repository.FileOrderRepository;
import com.ssemi.sampleorder.repository.FileProductionJobRepository;
import com.ssemi.sampleorder.repository.FileSampleRepository;
import com.ssemi.sampleorder.repository.FileSequenceRepository;
import com.ssemi.sampleorder.util.MutableTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowServiceTest {
    @TempDir
    Path tempDir;

    private FileSampleRepository sampleRepository;
    private FileOrderRepository orderRepository;
    private FileProductionJobRepository productionJobRepository;
    private FileSequenceRepository sequenceRepository;
    private MutableTimeProvider timeProvider;
    private SampleService sampleService;
    private ProductionService productionService;
    private OrderService orderService;
    private ReleaseService releaseService;
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        productionJobRepository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        sequenceRepository = new FileSequenceRepository(tempDir.resolve("sequences.json"));
        timeProvider = new MutableTimeProvider(LocalDateTime.of(2026, 6, 12, 9, 0));
        sampleService = new SampleService(sampleRepository);
        productionService = new ProductionService(sampleRepository, orderRepository, productionJobRepository, sequenceRepository, timeProvider);
        orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider, productionService);
        releaseService = new ReleaseService(sampleRepository, orderRepository, timeProvider);
        monitoringService = new MonitoringService(sampleRepository, orderRepository, productionJobRepository);
    }

    @Test
    void rejectsReservedOrder() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        Order order = orderService.reserveOrder("S-001", "AI Lab", 10);

        Order rejected = orderService.rejectOrder(order.id());

        assertEquals(OrderStatus.REJECTED, rejected.status());
        assertEquals(1, monitoringService.createSnapshot().rejectedOrderCount());
        assertEquals(0, monitoringService.createSnapshot().activeOrderCounts().get(OrderStatus.RESERVED));
    }

    @Test
    void approvesOrderAsConfirmedWhenAvailableStockIsEnough() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        Order order = orderService.reserveOrder("S-001", "AI Lab", 10);

        Order approved = orderService.approveOrder(order.id());

        assertEquals(OrderStatus.CONFIRMED, approved.status());
        assertEquals(10, approved.allocatedQuantity());
        assertEquals(470, monitoringService.createSnapshot().inventoryRows().get(0).availableStock());
    }

    @Test
    void approvesOrderAsProducingAndCreatesRunningProductionJobWhenStockIsShort() {
        sampleService.registerSample("S-003", "SiC 파워기판-6인치", 0.8, 0.92, 30);
        Order order = orderService.reserveOrder("S-003", "Power Lab", 40);

        Order approved = orderService.approveOrder(order.id());
        ProductionJob job = productionJobRepository.findAll().get(0);

        assertEquals(OrderStatus.PRODUCING, approved.status());
        assertEquals(30, approved.allocatedQuantity());
        assertEquals(10, job.shortageQuantity());
        assertEquals(13, job.plannedProductionQuantity());
        assertEquals(ProductionJobStatus.RUNNING, job.status());
    }

    @Test
    void processesProductionJobsInFifoOrderAndConfirmsOrders() {
        sampleService.registerSample("S-003", "SiC 파워기판-6인치", 0.8, 0.92, 0);
        Order first = orderService.reserveOrder("S-003", "Power Lab A", 10);
        Order second = orderService.reserveOrder("S-003", "Power Lab B", 5);
        orderService.approveOrder(first.id());
        orderService.approveOrder(second.id());

        assertEquals(ProductionJobStatus.RUNNING, productionJobRepository.findAll().get(0).status());
        assertEquals(ProductionJobStatus.WAITING, productionJobRepository.findAll().get(1).status());

        timeProvider.advanceMinutes(11);
        productionService.synchronizeProductionLine();

        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(first.id()).orElseThrow().status());
        assertEquals(OrderStatus.PRODUCING, orderRepository.findById(second.id()).orElseThrow().status());
        assertEquals(ProductionJobStatus.COMPLETED, productionJobRepository.findAll().get(0).status());
        assertEquals(ProductionJobStatus.RUNNING, productionJobRepository.findAll().get(1).status());
    }

    @Test
    void completesMultipleJobsWhenSufficientTimeElapses() {
        sampleService.registerSample("S-003", "SiC 파워기판-6인치", 0.8, 0.92, 0);
        Order first = orderService.reserveOrder("S-003", "Lab A", 10);
        Order second = orderService.reserveOrder("S-003", "Lab B", 5);
        orderService.approveOrder(first.id());  // JOB-0001 RUNNING (ceil(10/(0.92*0.9))=13개, 0.8*13=10.4분)
        orderService.approveOrder(second.id()); // JOB-0002 WAITING (ceil(5/(0.92*0.9))=7개, 0.8*7=5.6분)

        // 두 번째 작업은 첫 번째 완료 직후 시작 → 총 ~16분 후 모두 완료 가능
        timeProvider.advanceMinutes(20);
        productionService.synchronizeProductionLine();

        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(first.id()).orElseThrow().status());
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(second.id()).orElseThrow().status());
        ProductionJob job1 = productionJobRepository.findAll().get(0);
        ProductionJob job2 = productionJobRepository.findAll().get(1);
        assertEquals(ProductionJobStatus.COMPLETED, job1.status());
        assertEquals(ProductionJobStatus.COMPLETED, job2.status());
        // completedAt은 시스템 동기화 시각이 아닌 이론적 완료 시각(expectedEndAt)이어야 함
        assertEquals(job1.expectedEndAt(), job1.completedAt());
        assertEquals(job2.expectedEndAt(), job2.completedAt());
        // 두 번째 작업의 시작 시각은 첫 번째 작업의 완료 시각과 같아야 함
        assertEquals(job1.expectedEndAt(), job2.startedAt());
    }

    @Test
    void releasesConfirmedOrderAndDecreasesStock() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        Order order = orderService.reserveOrder("S-001", "AI Lab", 10);
        orderService.approveOrder(order.id());

        Order released = releaseService.releaseOrder(order.id());

        assertEquals(OrderStatus.RELEASE, released.status());
        assertEquals(0, released.allocatedQuantity());
        assertEquals(470, sampleRepository.findById("S-001").orElseThrow().stockQuantity());
        assertThrows(IllegalArgumentException.class, () -> releaseService.releaseOrder(order.id()));
    }

    @Test
    void listConfirmedOrdersReturnsOnlyConfirmed() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        orderService.reserveOrder("S-001", "Reserved Co", 5);
        Order toConfirm = orderService.reserveOrder("S-001", "Confirm Co", 5);
        orderService.approveOrder(toConfirm.id());
        Order toRelease = orderService.reserveOrder("S-001", "Release Co", 5);
        orderService.approveOrder(toRelease.id());
        releaseService.releaseOrder(toRelease.id());

        List<Order> confirmed = releaseService.listConfirmedOrders();

        assertEquals(1, confirmed.size());
        assertEquals(OrderStatus.CONFIRMED, confirmed.get(0).status());
    }

    @Test
    void activeOrdersByStatusListIsImmutable() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        orderService.reserveOrder("S-001", "AI Lab", 5);
        MonitoringSnapshot snapshot = monitoringService.createSnapshot();
        List<Order> list = snapshot.activeOrdersByStatus().get(OrderStatus.RESERVED);
        assertThrows(UnsupportedOperationException.class,
                () -> list.add(null), "activeOrdersByStatus value list는 불변이어야 함");
    }

    @Test
    void listProductionJobsByStatusForLineDisplay() {
        sampleService.registerSample("S-003", "SiC 파워기판-6인치", 0.8, 0.92, 0);
        orderService.approveOrder(orderService.reserveOrder("S-003", "Lab A", 10).id()); // RUNNING
        orderService.approveOrder(orderService.reserveOrder("S-003", "Lab B", 5).id());  // WAITING

        List<ProductionJob> running = productionService.listRunningJobs();
        List<ProductionJob> waiting = productionService.listWaitingJobs();

        assertEquals(1, running.size());
        assertEquals(ProductionJobStatus.RUNNING, running.get(0).status());
        assertEquals(1, waiting.size());
        assertEquals(ProductionJobStatus.WAITING, waiting.get(0).status());
    }

    @Test
    void snapshotContainsOrdersGroupedByStatus() {
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        Order reserved = orderService.reserveOrder("S-001", "AI Lab", 5);
        Order toConfirm = orderService.reserveOrder("S-001", "Fab B", 5);
        orderService.approveOrder(toConfirm.id());
        Order toReject = orderService.reserveOrder("S-001", "Reject Co", 3);
        orderService.rejectOrder(toReject.id());

        MonitoringSnapshot snapshot = monitoringService.createSnapshot();
        Map<OrderStatus, List<Order>> byStatus = snapshot.activeOrdersByStatus();

        assertEquals(1, byStatus.getOrDefault(OrderStatus.RESERVED, List.of()).size());
        assertEquals(reserved.id(), byStatus.get(OrderStatus.RESERVED).get(0).id());
        assertEquals(1, byStatus.getOrDefault(OrderStatus.CONFIRMED, List.of()).size());
        assertFalse(byStatus.containsKey(OrderStatus.REJECTED), "REJECTED는 activeOrdersByStatus에 포함되지 않아야 함");
    }
}
