package com.ssemi.sampleorder.repository;

public interface SequenceRepository {
    int nextOrderNumber();

    int nextProductionJobNumber();
}
