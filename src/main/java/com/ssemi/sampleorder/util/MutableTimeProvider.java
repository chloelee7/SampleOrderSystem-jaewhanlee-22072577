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
        if (minutes <= 0) {
            throw new IllegalArgumentException("경과 시간은 1분 이상이어야 합니다.");
        }
        currentTime = currentTime.plusMinutes(minutes);
    }
}
