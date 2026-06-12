package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.OrderStatus;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.repository.SequenceRepository;
import com.ssemi.sampleorder.util.IdGenerator;
import com.ssemi.sampleorder.util.TimeProvider;
import com.ssemi.sampleorder.util.ValidationUtils;

public class OrderService {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final SequenceRepository sequenceRepository;
    private final TimeProvider timeProvider;
    private final ProductionService productionService;

    public OrderService(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            SequenceRepository sequenceRepository,
            TimeProvider timeProvider
    ) {
        this(sampleRepository, orderRepository, sequenceRepository, timeProvider, null);
    }

    public OrderService(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            SequenceRepository sequenceRepository,
            TimeProvider timeProvider,
            ProductionService productionService
    ) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.sequenceRepository = sequenceRepository;
        this.timeProvider = timeProvider;
        this.productionService = productionService;
    }

    public Order reserveOrder(String sampleId, String customerName, int quantity) {
        String normalizedSampleId = ValidationUtils.requireText(sampleId, "시료 ID");
        if (sampleRepository.findById(normalizedSampleId).isEmpty()) {
            throw new IllegalArgumentException("등록되지 않은 시료 ID입니다: " + normalizedSampleId);
        }
        String orderId = IdGenerator.orderId(sequenceRepository.nextOrderNumber());
        Order order = Order.reserve(
                orderId,
                normalizedSampleId,
                customerName,
                ValidationUtils.requirePositive(quantity, "주문 수량"),
                timeProvider.now()
        );
        orderRepository.save(order);
        return order;
    }

    public Order rejectOrder(String orderId) {
        Order order = findOrder(orderId);
        requireStatus(order, OrderStatus.RESERVED, "RESERVED 상태 주문만 거절할 수 있습니다.");
        Order rejected = order.reject(timeProvider.now());
        orderRepository.update(rejected);
        return rejected;
    }

    public Order approveOrder(String orderId) {
        Order order = findOrder(orderId);
        requireStatus(order, OrderStatus.RESERVED, "RESERVED 상태 주문만 승인할 수 있습니다.");
        Sample sample = sampleRepository.findById(order.sampleId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시료 ID입니다: " + order.sampleId()));

        int availableStock = availableStock(sample);
        int allocatedQuantity = Math.min(availableStock, order.quantity());
        int shortageQuantity = order.quantity() - allocatedQuantity;

        if (shortageQuantity == 0) {
            Order confirmed = order.toConfirmed(allocatedQuantity, timeProvider.now());
            orderRepository.update(confirmed);
            return confirmed;
        }

        if (productionService == null) {
            throw new IllegalStateException("생산 서비스가 설정되지 않았습니다.");
        }
        Order producing = order.toProducing(allocatedQuantity, timeProvider.now());
        orderRepository.update(producing);
        productionService.enqueueProduction(producing, sample, shortageQuantity);
        return producing;
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(ValidationUtils.requireText(orderId, "주문 ID"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + orderId));
    }

    private void requireStatus(Order order, OrderStatus status, String message) {
        if (order.status() != status) {
            throw new IllegalArgumentException(message);
        }
    }

    private int availableStock(Sample sample) {
        int allocated = orderRepository.findAll().stream()
                .filter(order -> order.sampleId().equals(sample.id()))
                .filter(order -> order.status() == OrderStatus.CONFIRMED || order.status() == OrderStatus.PRODUCING)
                .mapToInt(Order::allocatedQuantity)
                .sum();
        return Math.max(0, sample.stockQuantity() - allocated);
    }
}
