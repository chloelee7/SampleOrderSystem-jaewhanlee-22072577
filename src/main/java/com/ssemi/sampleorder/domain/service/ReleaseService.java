package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.util.TimeProvider;
import com.ssemi.sampleorder.util.ValidationUtils;

import java.util.List;

public class ReleaseService {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final TimeProvider timeProvider;

    public ReleaseService(SampleRepository sampleRepository, OrderRepository orderRepository, TimeProvider timeProvider) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.timeProvider = timeProvider;
    }

    public List<Order> listConfirmedOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.status() == OrderStatus.CONFIRMED)
                .toList();
    }

    public Order releaseOrder(String orderId) {
        Order order = orderRepository.findById(ValidationUtils.requireText(orderId, "주문 ID"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + orderId));
        if (order.status() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("CONFIRMED 상태 주문만 출고할 수 있습니다.");
        }
        Sample sample = sampleRepository.findById(order.sampleId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시료 ID입니다: " + order.sampleId()));
        if (sample.stockQuantity() < order.quantity()) {
            throw new IllegalArgumentException("출고 가능한 재고가 부족합니다.");
        }

        sampleRepository.update(sample.withStockQuantity(sample.stockQuantity() - order.quantity()));
        Order released = order.toRelease(timeProvider.now());
        orderRepository.update(released);
        return released;
    }
}
