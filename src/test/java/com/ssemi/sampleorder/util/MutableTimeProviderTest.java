package com.ssemi.sampleorder.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MutableTimeProviderTest {
    private final LocalDateTime base = LocalDateTime.of(2026, 6, 12, 9, 0);

    @Test
    void advanceMinutesZeroThrows() {
        MutableTimeProvider provider = new MutableTimeProvider(base);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> provider.advanceMinutes(0));
        assertEquals("경과 시간은 1분 이상이어야 합니다.", ex.getMessage());
    }

    @Test
    void advanceMinutesNegativeThrows() {
        MutableTimeProvider provider = new MutableTimeProvider(base);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> provider.advanceMinutes(-1));
        assertEquals("경과 시간은 1분 이상이어야 합니다.", ex.getMessage());
    }

    @Test
    void advanceMinutesPositiveSucceeds() {
        MutableTimeProvider provider = new MutableTimeProvider(base);
        provider.advanceMinutes(1);
        assertEquals(base.plusMinutes(1), provider.now());
    }
}
