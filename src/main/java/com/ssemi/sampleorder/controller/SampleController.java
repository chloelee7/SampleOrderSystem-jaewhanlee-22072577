package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.service.SampleService;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class SampleController {
    private final SampleService sampleService;
    private final InputView inputView;
    private final OutputView outputView;

    public SampleController(SampleService sampleService, InputView inputView, OutputView outputView) {
        this.sampleService = sampleService;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        outputView.line("[1] 시료 등록");
        outputView.line("[2] 시료 목록");
        outputView.line("[3] 시료 검색");
        outputView.line("[0] 뒤로");
        int menu = inputView.readInt("선택: ");
        switch (menu) {
            case 1 -> register();
            case 2 -> TablePrinter.printSamples(sampleService.listSamples());
            case 3 -> search();
            case 0 -> outputView.line("메인 메뉴로 돌아갑니다.");
            default -> outputView.line("메뉴에 없는 번호입니다.");
        }
    }

    private void register() {
        sampleService.registerSample(
                inputView.readText("시료 ID: "),
                inputView.readText("시료명: "),
                inputView.readDouble("평균 생산시간(분): "),
                inputView.readDouble("수율: "),
                inputView.readInt("현재 재고: ")
        );
        outputView.line("시료 등록이 완료되었습니다.");
    }

    private void search() {
        String keyword = inputView.readText("검색어: ");
        TablePrinter.printSamples(sampleService.searchSamples(keyword));
    }
}
