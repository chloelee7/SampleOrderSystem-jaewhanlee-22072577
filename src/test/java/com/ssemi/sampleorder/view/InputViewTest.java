package com.ssemi.sampleorder.view;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputViewTest {

    private InputView inputViewOf(String input) {
        return new InputView(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    @Test
    void readIntWithNonNumericThrows() {
        InputView view = inputViewOf("abc\n");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> view.readInt("입력: "));
        assertEquals("정수를 입력해주세요.", ex.getMessage());
    }

    @Test
    void readDoubleWithNonNumericThrows() {
        InputView view = inputViewOf("abc\n");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> view.readDouble("입력: "));
        assertEquals("숫자를 입력해주세요.", ex.getMessage());
    }

    @Test
    void readIntWithEofThrowsConsoleInputClosedException() {
        InputView view = inputViewOf("");
        assertThrows(ConsoleInputClosedException.class, () -> view.readInt("입력: "));
    }
}
