package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.Comparator;
import java.util.List;

public class SampleService {
    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public Sample registerSample(
            String id,
            String name,
            double averageProductionTimeMinutes,
            double yieldRate,
            int stockQuantity
    ) {
        if (sampleRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 시료 ID입니다: " + id);
        }
        Sample sample = new Sample(id, name, averageProductionTimeMinutes, yieldRate, stockQuantity);
        sampleRepository.save(sample);
        return sample;
    }

    public List<Sample> listSamples() {
        return sampleRepository.findAll().stream()
                .sorted(Comparator.comparing(Sample::id))
                .toList();
    }

    public List<Sample> searchSamples(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        return sampleRepository.findAll().stream()
                .filter(sample -> sample.id().toLowerCase().contains(normalizedKeyword)
                        || sample.name().toLowerCase().contains(normalizedKeyword))
                .sorted(Comparator.comparing(Sample::id))
                .toList();
    }
}
