package com.ssemi.sampleorder.view;

public class ConsoleInputClosedException extends RuntimeException {
    public ConsoleInputClosedException() {
        super("입력이 종료되어 프로그램을 종료합니다.");
    }
}
