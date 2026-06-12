package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileOrderRepository implements OrderRepository {
    private final JsonFileStore<Order> store;

    public FileOrderRepository(Path file) {
        this.store = new JsonFileStore<>(file, Order[].class);
    }

    @Override
    public void save(Order order) {
        List<Order> orders = store.load();
        orders.add(order);
        store.saveAll(orders);
    }

    @Override
    public void update(Order order) {
        List<Order> orders = store.load();
        for (int index = 0; index < orders.size(); index++) {
            if (orders.get(index).id().equals(order.id())) {
                orders.set(index, order);
                store.saveAll(orders);
                return;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + order.id());
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.load());
    }

    @Override
    public Optional<Order> findById(String id) {
        return store.load().stream()
                .filter(order -> order.id().equals(id))
                .findFirst();
    }
}
