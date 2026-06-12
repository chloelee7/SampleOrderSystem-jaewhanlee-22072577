package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.ProductionJob;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record MonitoringSnapshot(
        Map<OrderStatus, Integer> activeOrderCounts,
        int rejectedOrderCount,
        List<InventoryRow> inventoryRows,
        List<ProductionJob> productionJobs,
        Map<OrderStatus, List<Order>> activeOrdersByStatus
) {
    public MonitoringSnapshot {
        activeOrderCounts = Map.copyOf(activeOrderCounts);
        inventoryRows = List.copyOf(inventoryRows);
        productionJobs = List.copyOf(productionJobs);
        Map<OrderStatus, List<Order>> deepCopy = new EnumMap<>(OrderStatus.class);
        activeOrdersByStatus.forEach((k, v) -> deepCopy.put(k, List.copyOf(v)));
        activeOrdersByStatus = Collections.unmodifiableMap(deepCopy);
    }
}
