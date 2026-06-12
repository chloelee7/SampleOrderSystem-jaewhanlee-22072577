package com.ssemi.sampleorder.domain.model;

import com.ssemi.sampleorder.util.ValidationUtils;

import java.time.LocalDateTime;

public record Order(
        String id,
        String sampleId,
        String customerName,
        int quantity,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int allocatedQuantity,
        LocalDateTime rejectedAt,
        LocalDateTime releasedAt
) {
    public Order {
        id = ValidationUtils.requireText(id, "주문 ID");
        sampleId = ValidationUtils.requireText(sampleId, "시료 ID");
        customerName = ValidationUtils.requireText(customerName, "고객명");
        quantity = ValidationUtils.requirePositive(quantity, "주문 수량");
        allocatedQuantity = ValidationUtils.requireNonNegative(allocatedQuantity, "할당 수량");
        if (allocatedQuantity > quantity) {
            throw new IllegalArgumentException("할당 수량은 주문 수량보다 클 수 없습니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("주문 생성/수정 시간은 필수입니다.");
        }
    }

    public static Order reserve(String id, String sampleId, String customerName, int quantity, LocalDateTime now) {
        return new Order(id, sampleId, customerName, quantity, OrderStatus.RESERVED, now, now, 0, null, null);
    }

    public Order reject(LocalDateTime now) {
        return new Order(id, sampleId, customerName, quantity, OrderStatus.REJECTED, createdAt, now, allocatedQuantity, now, releasedAt);
    }

    public Order toProducing(int allocatedQuantity, LocalDateTime now) {
        return new Order(id, sampleId, customerName, quantity, OrderStatus.PRODUCING, createdAt, now, allocatedQuantity, rejectedAt, releasedAt);
    }

    public Order toConfirmed(int allocatedQuantity, LocalDateTime now) {
        return new Order(id, sampleId, customerName, quantity, OrderStatus.CONFIRMED, createdAt, now, allocatedQuantity, rejectedAt, releasedAt);
    }

    public Order toRelease(LocalDateTime now) {
        return new Order(id, sampleId, customerName, quantity, OrderStatus.RELEASE, createdAt, now, 0, rejectedAt, now);
    }
}
