package com.ssemi.sampleorder.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionCalculatorTest {
    @Test
    void calculatesPlannedProductionQuantityAndTotalMinutes() {
        int planned = ProductionCalculator.plannedProductionQuantity(10, 0.92);

        assertEquals(13, planned);
        assertEquals(10.4, ProductionCalculator.totalProductionTimeMinutes(0.8, planned), 0.0001);
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductionCalculator.plannedProductionQuantity(0, 0.92));
        assertThrows(IllegalArgumentException.class,
                () -> ProductionCalculator.plannedProductionQuantity(10, 0));
    }
}
