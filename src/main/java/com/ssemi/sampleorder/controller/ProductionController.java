package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.ProductionJobStatus;
import com.ssemi.sampleorder.domain.service.ProductionService;
import com.ssemi.sampleorder.repository.ProductionJobRepository;
import com.ssemi.sampleorder.util.MutableTimeProvider;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class ProductionController {
    private final ProductionService productionService;
    private final ProductionJobRepository productionJobRepository;
    private final MutableTimeProvider timeProvider;
    private final InputView inputView;
    private final OutputView outputView;

    public ProductionController(
            ProductionService productionService,
            ProductionJobRepository productionJobRepository,
            MutableTimeProvider timeProvider,
            InputView inputView,
            OutputView outputView
    ) {
        this.productionService = productionService;
        this.productionJobRepository = productionJobRepository;
        this.timeProvider = timeProvider;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        outputView.line("[1] 현재 생산 현황 조회");
        outputView.line("[2] 생산 대기 큐 조회");
        outputView.line("[3] 시간 경과 처리");
        outputView.line("[0] 뒤로");
        int menu = inputView.readInt("선택: ");
        switch (menu) {
            case 1 -> TablePrinter.printProductionJobs(productionJobRepository.findAll().stream()
                    .filter(job -> job.status() == ProductionJobStatus.RUNNING)
                    .toList());
            case 2 -> TablePrinter.printProductionJobs(productionJobRepository.findAll().stream()
                    .filter(job -> job.status() == ProductionJobStatus.WAITING)
                    .toList());
            case 3 -> advanceTime();
            case 0 -> outputView.line("메인 메뉴로 돌아갑니다.");
            default -> outputView.line("메뉴에 없는 번호입니다.");
        }
    }

    private void advanceTime() {
        int minutes = inputView.readInt("경과 시간(분): ");
        timeProvider.advanceMinutes(minutes);
        productionService.synchronizeProductionLine();
        outputView.line("현재 시각: " + timeProvider.now());
    }
}
