package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);

    void update(Order order);

    List<Order> findAll();

    Optional<Order> findById(String id);
}
