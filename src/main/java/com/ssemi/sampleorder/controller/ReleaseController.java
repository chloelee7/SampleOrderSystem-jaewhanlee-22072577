package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.service.ReleaseService;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class ReleaseController {
    private final ReleaseService releaseService;
    private final InputView inputView;
    private final OutputView outputView;

    public ReleaseController(ReleaseService releaseService, InputView inputView, OutputView outputView) {
        this.releaseService = releaseService;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        TablePrinter.printOrders(releaseService.listConfirmedOrders());
        String orderId = inputView.readText("출고할 주문 ID: ");
        outputView.line("출고 완료: " + releaseService.releaseOrder(orderId).id());
    }
}
