package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.service.OrderService;
import com.ssemi.sampleorder.view.InputView;
import com.ssemi.sampleorder.view.OutputView;

public class OrderController {
    private final OrderService orderService;
    private final InputView inputView;
    private final OutputView outputView;

    public OrderController(OrderService orderService, InputView inputView, OutputView outputView) {
        this.orderService = orderService;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void reserve() {
        Order order = orderService.reserveOrder(
                inputView.readText("시료 ID: "),
                inputView.readText("고객명: "),
                inputView.readInt("주문 수량: ")
        );
        outputView.line("주문 접수가 완료되었습니다. 주문 ID: " + order.id() + ", 상태: " + order.status());
    }
}
