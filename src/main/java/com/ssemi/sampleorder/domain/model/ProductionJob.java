package com.ssemi.sampleorder.domain.model;

import com.ssemi.sampleorder.util.ProductionCalculator;
import com.ssemi.sampleorder.util.ValidationUtils;

import java.time.LocalDateTime;

public record ProductionJob(
        String id,
        String orderId,
        String sampleId,
        int shortageQuantity,
        int plannedProductionQuantity,
        double averageProductionTimeMinutes,
        double totalProductionTimeMinutes,
        ProductionJobStatus status,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime expectedEndAt,
        LocalDateTime completedAt
) {
    public ProductionJob {
        id = ValidationUtils.requireText(id, "생산 작업 ID");
        orderId = ValidationUtils.requireText(orderId, "주문 ID");
        sampleId = ValidationUtils.requireText(sampleId, "시료 ID");
        shortageQuantity = ValidationUtils.requirePositive(shortageQuantity, "부족 수량");
        plannedProductionQuantity = ValidationUtils.requirePositive(plannedProductionQuantity, "계획 생산 수량");
        averageProductionTimeMinutes = ValidationUtils.requirePositive(averageProductionTimeMinutes, "평균 생산시간");
        totalProductionTimeMinutes = ValidationUtils.requirePositive(totalProductionTimeMinutes, "총 생산시간");
        if (status == null) {
            throw new IllegalArgumentException("생산 작업 상태는 필수입니다.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생산 작업 생성 시간은 필수입니다.");
        }
    }

    public static ProductionJob waiting(
            String id,
            String orderId,
            String sampleId,
            int shortageQuantity,
            int plannedProductionQuantity,
            double averageProductionTimeMinutes,
            LocalDateTime now
    ) {
        double totalProductionTimeMinutes = ProductionCalculator.totalProductionTimeMinutes(
                averageProductionTimeMinutes,
                plannedProductionQuantity
        );
        return new ProductionJob(
                id,
                orderId,
                sampleId,
                shortageQuantity,
                plannedProductionQuantity,
                averageProductionTimeMinutes,
                totalProductionTimeMinutes,
                ProductionJobStatus.WAITING,
                now,
                null,
                null,
                null
        );
    }

    public ProductionJob start(LocalDateTime now) {
        LocalDateTime expectedEndAt = now.plusMinutes((long) Math.ceil(totalProductionTimeMinutes));
        return new ProductionJob(
                id,
                orderId,
                sampleId,
                shortageQuantity,
                plannedProductionQuantity,
                averageProductionTimeMinutes,
                totalProductionTimeMinutes,
                ProductionJobStatus.RUNNING,
                createdAt,
                now,
                expectedEndAt,
                completedAt
        );
    }

    public ProductionJob complete(LocalDateTime now) {
        return new ProductionJob(
                id,
                orderId,
                sampleId,
                shortageQuantity,
                plannedProductionQuantity,
                averageProductionTimeMinutes,
                totalProductionTimeMinutes,
                ProductionJobStatus.COMPLETED,
                createdAt,
                startedAt,
                expectedEndAt,
                now
        );
    }
}
