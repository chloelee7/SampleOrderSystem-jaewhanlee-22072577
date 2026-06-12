package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileSampleRepository implements SampleRepository {
    private final JsonFileStore<Sample> store;

    public FileSampleRepository(Path file) {
        this.store = new JsonFileStore<>(file, Sample[].class);
    }

    @Override
    public void save(Sample sample) {
        List<Sample> samples = store.load();
        samples.add(sample);
        store.saveAll(samples);
    }

    @Override
    public void update(Sample sample) {
        List<Sample> samples = store.load();
        for (int index = 0; index < samples.size(); index++) {
            if (samples.get(index).id().equals(sample.id())) {
                samples.set(index, sample);
                store.saveAll(samples);
                return;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 시료 ID입니다: " + sample.id());
    }

    @Override
    public List<Sample> findAll() {
        return List.copyOf(store.load());
    }

    @Override
    public Optional<Sample> findById(String id) {
        return store.load().stream()
                .filter(sample -> sample.id().equals(id))
                .findFirst();
    }
}
