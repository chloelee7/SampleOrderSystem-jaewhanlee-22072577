package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.ProductionJob;

import java.util.List;
import java.util.Map;

public record MonitoringSnapshot(
        Map<OrderStatus, Integer> activeOrderCounts,
        int rejectedOrderCount,
        List<InventoryRow> inventoryRows,
        List<ProductionJob> productionJobs
) {
    public MonitoringSnapshot {
        activeOrderCounts = Map.copyOf(activeOrderCounts);
        inventoryRows = List.copyOf(inventoryRows);
        productionJobs = List.copyOf(productionJobs);
    }
}
