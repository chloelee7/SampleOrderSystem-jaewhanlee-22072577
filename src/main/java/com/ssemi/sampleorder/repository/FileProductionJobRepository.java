package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileProductionJobRepository implements ProductionJobRepository {
    private final JsonFileStore<ProductionJob> store;

    public FileProductionJobRepository(Path file) {
        this.store = new JsonFileStore<>(file, ProductionJob[].class);
    }

    @Override
    public void save(ProductionJob job) {
        List<ProductionJob> jobs = store.load();
        jobs.add(job);
        store.saveAll(jobs);
    }

    @Override
    public void update(ProductionJob job) {
        List<ProductionJob> jobs = store.load();
        for (int index = 0; index < jobs.size(); index++) {
            if (jobs.get(index).id().equals(job.id())) {
                jobs.set(index, job);
                store.saveAll(jobs);
                return;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 생산 작업 ID입니다: " + job.id());
    }

    @Override
    public List<ProductionJob> findAll() {
        return List.copyOf(store.load());
    }

    @Override
    public Optional<ProductionJob> findById(String id) {
        return store.load().stream()
                .filter(job -> job.id().equals(id))
                .findFirst();
    }
}
