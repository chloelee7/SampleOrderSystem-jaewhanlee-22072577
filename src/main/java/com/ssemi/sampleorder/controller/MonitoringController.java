package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.service.MonitoringService;
import com.ssemi.sampleorder.domain.service.MonitoringSnapshot;
import com.ssemi.sampleorder.view.OutputView;
import com.ssemi.sampleorder.view.TablePrinter;

public class MonitoringController {
    private final MonitoringService monitoringService;
    private final OutputView outputView;

    public MonitoringController(MonitoringService monitoringService, OutputView outputView) {
        this.monitoringService = monitoringService;
        this.outputView = outputView;
    }

    public void run() {
        MonitoringSnapshot snapshot = monitoringService.createSnapshot();
        outputView.line("주문량 확인");
        for (OrderStatus status : OrderStatus.values()) {
            if (status != OrderStatus.REJECTED) {
                outputView.line(status + ": " + snapshot.activeOrderCounts().getOrDefault(status, 0));
            }
        }
        outputView.line("REJECTED(집계 제외): " + snapshot.rejectedOrderCount());
        outputView.line("재고량 확인");
        TablePrinter.printInventoryRows(snapshot.inventoryRows());
    }
}
