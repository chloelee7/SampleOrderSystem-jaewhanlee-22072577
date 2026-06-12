package com.ssemi.sampleorder.domain.model;

import com.ssemi.sampleorder.util.ValidationUtils;

public record SequenceState(int nextOrderNumber, int nextProductionJobNumber) {
    public SequenceState {
        nextOrderNumber = ValidationUtils.requirePositive(nextOrderNumber, "다음 주문 번호");
        nextProductionJobNumber = ValidationUtils.requirePositive(nextProductionJobNumber, "다음 생산 작업 번호");
    }

    public static SequenceState initial() {
        return new SequenceState(1, 1);
    }

    public SequenceState withNextOrderNumber(int value) {
        return new SequenceState(value, nextProductionJobNumber);
    }

    public SequenceState withNextProductionJobNumber(int value) {
        return new SequenceState(nextOrderNumber, value);
    }
}
