package com.ssemi.sampleorder.util;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }
        return value.trim();
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + "은(는) 1 이상이어야 합니다.");
        }
        return value;
    }

    public static double requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + "은(는) 0보다 커야 합니다.");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + "은(는) 0 이상이어야 합니다.");
        }
        return value;
    }

    public static double requireYieldRate(double value) {
        if (value <= 0 || value > 1) {
            throw new IllegalArgumentException("수율은 0보다 크고 1 이하여야 합니다.");
        }
        return value;
    }
}
