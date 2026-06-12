package com.ssemi.sampleorder.app;

import com.ssemi.sampleorder.controller.MainController;
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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Application {
    public void run(String[] args) {
        List<String> arguments = Arrays.asList(args);
        Path dataDirectory = dataDirectory(arguments);

        FileSampleRepository sampleRepository = new FileSampleRepository(dataDirectory.resolve("samples.json"));
        FileOrderRepository orderRepository = new FileOrderRepository(dataDirectory.resolve("orders.json"));
        FileProductionJobRepository productionJobRepository = new FileProductionJobRepository(dataDirectory.resolve("production_jobs.json"));
        FileSequenceRepository sequenceRepository = new FileSequenceRepository(dataDirectory.resolve("sequences.json"));
        MutableTimeProvider timeProvider = new MutableTimeProvider(LocalDateTime.now().withSecond(0).withNano(0));

        DummyDataService dummyDataService = new DummyDataService(sampleRepository, orderRepository, productionJobRepository);
        if (arguments.contains("--seed-dummy")) {
            dummyDataService.generateIfMissing();
        }

        ProductionService productionService = new ProductionService(sampleRepository, orderRepository, productionJobRepository, sequenceRepository, timeProvider);
        SampleService sampleService = new SampleService(sampleRepository);
        OrderService orderService = new OrderService(sampleRepository, orderRepository, sequenceRepository, timeProvider, productionService);
        ReleaseService releaseService = new ReleaseService(sampleRepository, orderRepository, timeProvider);
        MonitoringService monitoringService = new MonitoringService(sampleRepository, orderRepository, productionJobRepository);

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
                new InputView(new Scanner(System.in)),
                new OutputView()
        );
        controller.run();
    }

    private Path dataDirectory(List<String> arguments) {
        int index = arguments.indexOf("--data-dir");
        if (index >= 0 && index + 1 < arguments.size()) {
            return Path.of(arguments.get(index + 1));
        }
        return Path.of("data");
    }
}
