package com.ssemi.sampleorder.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 12, 9, 0);

    @Test
    void createsReservedOrder() {
        Order order = Order.reserve("ORD-0001", "S-001", "AI Lab", 5, NOW);

        assertEquals(OrderStatus.RESERVED, order.status());
        assertEquals(0, order.allocatedQuantity());
        assertEquals(NOW, order.createdAt());
        assertNull(order.rejectedAt());
        assertNull(order.releasedAt());
    }

    @Test
    void changesStatusWithTimestampsAndAllocation() {
        Order order = Order.reserve("ORD-0001", "S-001", "AI Lab", 5, NOW);

        Order producing = order.toProducing(2, NOW.plusMinutes(1));
        Order confirmed = producing.toConfirmed(5, NOW.plusMinutes(10));
        Order released = confirmed.toRelease(NOW.plusMinutes(20));

        assertEquals(OrderStatus.PRODUCING, producing.status());
        assertEquals(2, producing.allocatedQuantity());
        assertEquals(OrderStatus.CONFIRMED, confirmed.status());
        assertEquals(5, confirmed.allocatedQuantity());
        assertEquals(OrderStatus.RELEASE, released.status());
        assertEquals(0, released.allocatedQuantity());
        assertEquals(NOW.plusMinutes(20), released.releasedAt());
    }
}
