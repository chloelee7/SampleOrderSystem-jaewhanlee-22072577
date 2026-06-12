package com.ssemi.sampleorder.view;

import java.util.Scanner;

public class InputView {
    private final Scanner scanner;

    public InputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt) {
        return Integer.parseInt(readLine(prompt));
    }

    public double readDouble(String prompt) {
        return Double.parseDouble(readLine(prompt));
    }

    public String readText(String prompt) {
        return readLine(prompt);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new ConsoleInputClosedException();
        }
        return scanner.nextLine().trim();
    }
}
