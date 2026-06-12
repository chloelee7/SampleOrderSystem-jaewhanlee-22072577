package com.ssemi.sampleorder.view;

public class OutputView {
    public void line(String message) {
        System.out.println(message);
    }

    public void error(Exception exception) {
        System.out.println("[오류] " + exception.getMessage());
    }
}
