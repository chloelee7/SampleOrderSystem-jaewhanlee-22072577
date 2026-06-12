package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.ProductionJob;

import java.util.List;
import java.util.Optional;

public interface ProductionJobRepository {
    void save(ProductionJob job);

    void update(ProductionJob job);

    List<ProductionJob> findAll();

    Optional<ProductionJob> findById(String id);
}
