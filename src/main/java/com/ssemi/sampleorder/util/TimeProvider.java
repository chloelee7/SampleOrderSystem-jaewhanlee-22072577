package com.ssemi.sampleorder.util;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
}
