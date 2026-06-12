package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.service.DummyDataService;
import com.ssemi.sampleorder.domain.service.MonitoringService;
import com.ssemi.sampleorder.domain.service.OrderService;
import com.ssemi.sampleorder.domain.service.ProductionService;
import com.ssemi.sampleorder.domain.service.ReleaseService;
import com.ssemi.sampleorder.domain.service.SampleService;
import com.ssemi.sampleorder.repository.FileOrderRepository;
import com.ssemi.sampleorder.repository.FileProductionJobRepository;
import com.ssemi.sampleorder.repository.FileSampleRepository;
import com.ssemi.sampleorder.repository.FileSequenceRepository;
import com.ssemi.sampleorder.util.MutableTimeProvider;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainControllerTest {
    @TempDir
    Path tempDir;

    @Test
    void exitsSafelyWhenInputStreamEnds() {
        FileSampleRepository sampleRepository = new FileSampleRepository(tempDir.resolve("samples.json"));
        FileOrderRepository orderRepository = new FileOrderRepository(tempDir.resolve("orders.json"));
        FileProductionJobRepository productionJobRepository = new FileProductionJobRepository(tempDir.resolve("production_jobs.json"));
        FileSequenceRepository sequenceRepository = new FileSequenceRepository(tempDir.resolve("sequences.json"));
        MutableTimeProvider timeProvider = new MutableTimeProvider(LocalDateTime.of(2026, 6, 12, 9, 0));

        ProductionService productionService = new ProductionService(sampleRepository, orderRepository, productionJobRepository, sequenceRepository, timeProvider);
        SampleService sampleService = new SampleService(sampleRepository);
        OrderService orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider, productionService);
        ReleaseService releaseService = new ReleaseService(sampleRepository, orderRepository, timeProvider);
        MonitoringService monitoringService = new MonitoringService(sampleRepository, orderRepository, productionJobRepository);
        DummyDataService dummyDataService = new DummyDataService(sampleRepository, orderRepository, productionJobRepository);
        CapturingOutputView outputView = new CapturingOutputView();

        MainController controller = new MainController(
                sampleRepository,
                orderRepository,
                productionJobRepository,
                timeProvider,
                sampleService,
                orderService,
                productionService,
                releaseService,
                monitoringService,
                dummyDataService,
                new InputView(new Scanner("")),
                outputView
        );

        assertTimeoutPreemptively(Duration.ofSeconds(1), controller::run);

        assertTrue(outputView.sawShutdownMessage);
    }

    private static class CapturingOutputView extends OutputView {
        private int retryableErrorCount;
        private boolean sawShutdownMessage;

        @Override
        public void line(String message) {
            if (message.contains("입력이 종료되어 프로그램을 종료합니다.")) {
                sawShutdownMessage = true;
            }
        }

        @Override
        public void error(Exception exception) {
            retryableErrorCount++;
            if (retryableErrorCount > 1) {
                throw new AssertionError("EOF input should not be retried as a recoverable menu error.");
            }
        }
    }
}
