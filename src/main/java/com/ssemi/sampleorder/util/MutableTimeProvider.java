package com.ssemi.sampleorder.util;

import java.time.LocalDateTime;

public class MutableTimeProvider implements TimeProvider {
    private LocalDateTime currentTime;

    public MutableTimeProvider(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public LocalDateTime now() {
        return currentTime;
    }

    public void advanceMinutes(long minutes) {
        currentTime = currentTime.plusMinutes(minutes);
    }
}
