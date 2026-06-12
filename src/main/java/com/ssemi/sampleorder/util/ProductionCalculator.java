package com.ssemi.sampleorder.util;

public final class ProductionCalculator {
    private static final double YIELD_SAFETY_FACTOR = 0.9;

    private ProductionCalculator() {
    }

    public static int plannedProductionQuantity(int shortageQuantity, double yieldRate) {
        ValidationUtils.requirePositive(shortageQuantity, "부족 수량");
        ValidationUtils.requireYieldRate(yieldRate);
        return (int) Math.ceil(shortageQuantity / (yieldRate * YIELD_SAFETY_FACTOR));
    }

    public static double totalProductionTimeMinutes(double averageProductionTimeMinutes, int plannedProductionQuantity) {
        ValidationUtils.requirePositive(averageProductionTimeMinutes, "평균 생산시간");
        ValidationUtils.requirePositive(plannedProductionQuantity, "계획 생산 수량");
        return averageProductionTimeMinutes * plannedProductionQuantity;
    }
}
