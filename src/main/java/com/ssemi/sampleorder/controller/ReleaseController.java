package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.service.ReleaseService;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class ReleaseController {
    private final ReleaseService releaseService;
    private final OrderRepository orderRepository;
    private final InputView inputView;
    private final OutputView outputView;

    public ReleaseController(ReleaseService releaseService, OrderRepository orderRepository, InputView inputView, OutputView outputView) {
        this.releaseService = releaseService;
        this.orderRepository = orderRepository;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        TablePrinter.printOrders(orderRepository.findAll().stream()
                .filter(order -> order.status() == OrderStatus.CONFIRMED)
                .toList());
        String orderId = inputView.readText("출고할 주문 ID: ");
        outputView.line("출고 완료: " + releaseService.releaseOrder(orderId).id());
    }
}
