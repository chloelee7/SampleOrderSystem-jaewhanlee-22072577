package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.Sample;

import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    void save(Sample sample);

    void update(Sample sample);

    List<Sample> findAll();

    Optional<Sample> findById(String id);
}
