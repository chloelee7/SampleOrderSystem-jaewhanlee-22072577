package com.ssemi.sampleorder.domain.service;

public record InventoryRow(
        String sampleId,
        String sampleName,
        int stockQuantity,
        int allocatedQuantity,
        int availableStock,
        int openOrderQuantity,
        InventoryStatus status
) {
}
