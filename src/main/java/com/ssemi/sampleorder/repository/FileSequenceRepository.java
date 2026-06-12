package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.domain.model.SequenceState;
import com.ssemi.sampleorder.persistence.JsonObjectStore;

import java.nio.file.Path;

public class FileSequenceRepository implements SequenceRepository {
    private final JsonObjectStore<SequenceState> store;

    public FileSequenceRepository(Path file) {
        this.store = new JsonObjectStore<>(file, SequenceState.class);
    }

    @Override
    public int nextOrderNumber() {
        SequenceState state = load();
        store.save(state.withNextOrderNumber(state.nextOrderNumber() + 1));
        return state.nextOrderNumber();
    }

    @Override
    public int nextProductionJobNumber() {
        SequenceState state = load();
        store.save(state.withNextProductionJobNumber(state.nextProductionJobNumber() + 1));
        return state.nextProductionJobNumber();
    }

    private SequenceState load() {
        return store.load().orElse(SequenceState.initial());
    }
}
