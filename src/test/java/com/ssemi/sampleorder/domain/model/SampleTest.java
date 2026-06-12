package com.ssemi.sampleorder.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleTest {
    @Test
    void createsValidSampleAndUpdatesStock() {
        Sample sample = new Sample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480);

        Sample updated = sample.withStockQuantity(500);

        assertEquals("S-001", updated.id());
        assertEquals(500, updated.stockQuantity());
    }

    @Test
    void rejectsBlankNameInvalidYieldAndNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("S-001", " ", 0.5, 0.92, 480));
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("S-001", "실리콘 웨이퍼-8인치", 0, 0.92, 480));
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("S-001", "실리콘 웨이퍼-8인치", 0.5, 1.1, 480));
        assertThrows(IllegalArgumentException.class,
                () -> new Sample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, -1));
    }
}
