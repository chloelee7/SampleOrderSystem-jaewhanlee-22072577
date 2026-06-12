package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.Sample;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleAndOrderServiceTest {
    @TempDir
    Path tempDir;

    private FileSampleRepository sampleRepository;
    private FileOrderRepository orderRepository;
    private FileSequenceRepository sequenceRepository;
    private MutableTimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        sequenceRepository = new FileSequenceRepository(tempDir.resolve("sequences.json"));
        timeProvider = new MutableTimeProvider(LocalDateTime.of(2026, 6, 12, 9, 0));
    }

    @Test
    void registersSampleAndPreventsDuplicateId() {
        SampleService service = new SampleService(sampleRepository);

        Sample sample = service.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);

        assertEquals(sample, sampleRepository.findById("S-001").orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> service.registerSample("S-001", "중복 시료", 0.4, 0.9, 10));
    }

    @Test
    void searchesSamplesByIdOrName() {
        SampleService service = new SampleService(sampleRepository);
        service.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        service.registerSample("S-002", "GaN 에피택셜-4인치", 0.3, 0.78, 220);

        assertEquals(List.of("S-001"), service.searchSamples("S-001").stream().map(Sample::id).toList());
        assertEquals(List.of("S-002"), service.searchSamples("GaN").stream().map(Sample::id).toList());
    }

    @Test
    void reservesOrderForRegisteredSample() {
        SampleService sampleService = new SampleService(sampleRepository);
        sampleService.registerSample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);
        OrderService orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider);

        Order order = orderService.reserveOrder("S-001", "AI Lab", 10);

        assertEquals("ORD-0001", order.id());
        assertEquals(OrderStatus.RESERVED, order.status());
        assertEquals(timeProvider.now(), order.createdAt());
        assertEquals(order, orderRepository.findById("ORD-0001").orElseThrow());
    }

    @Test
    void rejectsOrderForUnknownSample() {
        OrderService orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.reserveOrder("S-999", "AI Lab", 10));
    }
}
