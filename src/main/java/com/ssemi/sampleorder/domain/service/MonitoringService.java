package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionJobRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonitoringService {
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.RESERVED,
            OrderStatus.PRODUCING,
            OrderStatus.CONFIRMED,
            OrderStatus.RELEASE
    );

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionJobRepository productionJobRepository;

    public MonitoringService(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            ProductionJobRepository productionJobRepository
    ) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionJobRepository = productionJobRepository;
    }

    public MonitoringSnapshot createSnapshot() {
        List<Order> orders = orderRepository.findAll();
        EnumMap<OrderStatus, Integer> activeCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : ACTIVE_STATUSES) {
            activeCounts.put(status, 0);
        }

        int rejectedCount = 0;
        for (Order order : orders) {
            if (order.status() == OrderStatus.REJECTED) {
                rejectedCount++;
            } else if (activeCounts.containsKey(order.status())) {
                activeCounts.merge(order.status(), 1, Integer::sum);
            }
        }

        List<InventoryRow> inventoryRows = sampleRepository.findAll().stream()
                .map(sample -> toInventoryRow(sample, orders))
                .toList();

        Map<OrderStatus, List<Order>> byStatus = orders.stream()
                .filter(o -> ACTIVE_STATUSES.contains(o.status()))
                .collect(Collectors.groupingBy(Order::status));

        return new MonitoringSnapshot(activeCounts, rejectedCount, inventoryRows, productionJobRepository.findAll(), byStatus);
    }

    private InventoryRow toInventoryRow(Sample sample, List<Order> orders) {
        int allocatedQuantity = orders.stream()
                .filter(order -> order.sampleId().equals(sample.id()))
                .filter(order -> order.status() == OrderStatus.CONFIRMED || order.status() == OrderStatus.PRODUCING)
                .mapToInt(Order::allocatedQuantity)
                .sum();
        int availableStock = Math.max(0, sample.stockQuantity() - allocatedQuantity);
        int openOrderQuantity = orders.stream()
                .filter(order -> order.sampleId().equals(sample.id()))
                .filter(order -> order.status() == OrderStatus.RESERVED
                        || order.status() == OrderStatus.PRODUCING
                        || order.status() == OrderStatus.CONFIRMED)
                .mapToInt(Order::quantity)
                .sum();
        return new InventoryRow(
                sample.id(),
                sample.name(),
                sample.stockQuantity(),
                allocatedQuantity,
                availableStock,
                openOrderQuantity,
                inventoryStatus(availableStock, openOrderQuantity)
        );
    }

    private InventoryStatus inventoryStatus(int availableStock, int openOrderQuantity) {
        if (availableStock == 0) {
            return InventoryStatus.고갈;
        }
        if (availableStock < openOrderQuantity) {
            return InventoryStatus.부족;
        }
        return InventoryStatus.여유;
    }
}
