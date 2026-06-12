package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.ProductionJobStatus;
import com.ssemi.sampleorder.domain.service.DummyDataService;
import com.ssemi.sampleorder.domain.service.MonitoringService;
import com.ssemi.sampleorder.domain.service.OrderService;
import com.ssemi.sampleorder.domain.service.ProductionService;
import com.ssemi.sampleorder.domain.service.ReleaseService;
import com.ssemi.sampleorder.domain.service.SampleService;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionJobRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.util.MutableTimeProvider;
import com.ssemi.sampleorder.view.ConsoleInputClosedException;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;

public class MainController {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionJobRepository productionJobRepository;
    private final MutableTimeProvider timeProvider;
    private final SampleController sampleController;
    private final OrderController orderController;
    private final ApprovalController approvalController;
    private final MonitoringController monitoringController;
    private final ProductionController productionController;
    private final ReleaseController releaseController;
    private final DummyDataService dummyDataService;
    private final InputView inputView;
    private final OutputView outputView;

    public MainController(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            ProductionJobRepository productionJobRepository,
            MutableTimeProvider timeProvider,
            SampleService sampleService,
            OrderService orderService,
            ProductionService productionService,
            ReleaseService releaseService,
            MonitoringService monitoringService,
            DummyDataService dummyDataService,
            InputView inputView,
            OutputView outputView
    ) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionJobRepository = productionJobRepository;
        this.timeProvider = timeProvider;
        this.sampleController = new SampleController(sampleService, inputView, outputView);
        this.orderController = new OrderController(orderService, inputView, outputView);
        this.approvalController = new ApprovalController(orderService, orderRepository, inputView, outputView);
        this.monitoringController = new MonitoringController(monitoringService, outputView);
        this.productionController = new ProductionController(productionService, productionJobRepository, timeProvider, inputView, outputView);
        this.releaseController = new ReleaseController(releaseService, orderRepository, inputView, outputView);
        this.dummyDataService = dummyDataService;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printSummary();
                printMainMenu();
                int menu = inputView.readInt("선택: ");
                switch (menu) {
                    case 1 -> sampleController.run();
                    case 2 -> orderController.reserve();
                    case 3 -> approvalController.run();
                    case 4 -> monitoringController.run();
                    case 5 -> productionController.run();
                    case 6 -> releaseController.run();
                    case 7 -> {
                        dummyDataService.generateIfMissing();
                        outputView.line("Dummy Data 생성이 완료되었습니다.");
                    }
                    case 0 -> running = false;
                    default -> outputView.line("메뉴에 없는 번호입니다.");
                }
            } catch (ConsoleInputClosedException exception) {
                outputView.line(exception.getMessage());
                running = false;
            } catch (RuntimeException exception) {
                outputView.error(exception);
            }
        }
    }

    private void printSummary() {
        int totalStock = sampleRepository.findAll().stream().mapToInt(sample -> sample.stockQuantity()).sum();
        long waitingJobs = productionJobRepository.findAll().stream()
                .filter(job -> job.status() == ProductionJobStatus.WAITING)
                .count();
        outputView.line("");
        outputView.line("=== S-Semi 반도체 시료 생산주문관리 시스템 ===");
        outputView.line("등록 시료 수: " + sampleRepository.findAll().size()
                + " | 총 재고 수량: " + totalStock
                + " | 전체 주문 수: " + orderRepository.findAll().size()
                + " | 생산라인 대기 작업 수: " + waitingJobs
                + " | 현재 시각: " + timeProvider.now());
    }

    private void printMainMenu() {
        outputView.line("[1] 시료 관리");
        outputView.line("[2] 시료 주문");
        outputView.line("[3] 주문 승인/거절");
        outputView.line("[4] 모니터링");
        outputView.line("[5] 생산라인 조회");
        outputView.line("[6] 출고 처리");
        outputView.line("[7] Dummy Data 생성");
        outputView.line("[0] 종료");
    }
}
