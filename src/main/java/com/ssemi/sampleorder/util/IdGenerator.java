package com.ssemi.sampleorder.util;

public final class IdGenerator {
    private IdGenerator() {
    }

    public static String orderId(int number) {
        return "ORD-%04d".formatted(number);
    }

    public static String productionJobId(int number) {
        return "JOB-%04d".formatted(number);
    }
}
