package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.service.OrderService;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class ApprovalController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InputView inputView;
    private final OutputView outputView;

    public ApprovalController(OrderService orderService, OrderRepository orderRepository, InputView inputView, OutputView outputView) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        TablePrinter.printOrders(orderRepository.findAll().stream()
                .filter(order -> order.status() == OrderStatus.RESERVED)
                .toList());
        outputView.line("[1] 승인");
        outputView.line("[2] 거절");
        outputView.line("[0] 뒤로");
        int menu = inputView.readInt("선택: ");
        if (menu == 0) {
            return;
        }
        String orderId = inputView.readText("주문 ID: ");
        if (menu == 1) {
            outputView.line("승인 결과: " + orderService.approveOrder(orderId).status());
        } else if (menu == 2) {
            outputView.line("거절 결과: " + orderService.rejectOrder(orderId).status());
        } else {
            outputView.line("메뉴에 없는 번호입니다.");
        }
    }
}
