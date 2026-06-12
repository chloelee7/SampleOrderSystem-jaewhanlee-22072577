package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.domain.service.InventoryRow;

import java.util.List;

public final class TablePrinter {
    private TablePrinter() {
    }

    public static void printSamples(List<Sample> samples) {
        System.out.printf("%-8s %-24s %8s %8s %8s%n", "ID", "이름", "시간", "수율", "재고");
        for (Sample sample : samples) {
            System.out.printf("%-8s %-24s %8.2f %8.2f %8d%n",
                    sample.id(), sample.name(), sample.averageProductionTimeMinutes(), sample.yieldRate(), sample.stockQuantity());
        }
    }

    public static void printOrders(List<Order> orders) {
        System.out.printf("%-10s %-8s %-12s %8s %-10s %8s%n", "주문ID", "시료ID", "고객", "수량", "상태", "할당");
        for (Order order : orders) {
            System.out.printf("%-10s %-8s %-12s %8d %-10s %8d%n",
                    order.id(), order.sampleId(), order.customerName(), order.quantity(), order.status(), order.allocatedQuantity());
        }
    }

    public static void printInventoryRows(List<InventoryRow> rows) {
        System.out.printf("%-8s %-24s %8s %8s %8s %8s %-6s%n", "ID", "이름", "물리재고", "할당", "가용", "미출고", "상태");
        for (InventoryRow row : rows) {
            System.out.printf("%-8s %-24s %8d %8d %8d %8d %-6s%n",
                    row.sampleId(), row.sampleName(), row.stockQuantity(), row.allocatedQuantity(),
                    row.availableStock(), row.openOrderQuantity(), row.status());
        }
    }

    public static void printProductionJobs(List<ProductionJob> jobs) {
        System.out.printf("%-10s %-10s %-8s %8s %8s %-10s%n", "작업ID", "주문ID", "시료ID", "부족", "계획", "상태");
        for (ProductionJob job : jobs) {
            System.out.printf("%-10s %-10s %-8s %8d %8d %-10s%n",
                    job.id(), job.orderId(), job.sampleId(), job.shortageQuantity(), job.plannedProductionQuantity(), job.status());
        }
    }
}
