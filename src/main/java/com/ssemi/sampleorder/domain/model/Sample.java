package com.ssemi.sampleorder.domain.model;

import com.ssemi.sampleorder.util.ValidationUtils;

public record Sample(
        String id,
        String name,
        double averageProductionTimeMinutes,
        double yieldRate,
        int stockQuantity
) {
    public Sample {
        id = ValidationUtils.requireText(id, "시료 ID");
        name = ValidationUtils.requireText(name, "시료명");
        averageProductionTimeMinutes = ValidationUtils.requirePositive(averageProductionTimeMinutes, "평균 생산시간");
        yieldRate = ValidationUtils.requireYieldRate(yieldRate);
        stockQuantity = ValidationUtils.requireNonNegative(stockQuantity, "재고 수량");
    }

    public Sample withStockQuantity(int newStockQuantity) {
        return new Sample(id, name, averageProductionTimeMinutes, yieldRate, newStockQuantity);
    }
}
