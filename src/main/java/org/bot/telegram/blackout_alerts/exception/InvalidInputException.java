package org.bot.telegram.blackout_alerts.exception;

import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {

    private static final String MESSAGE = """
        ❗ Введене не коректне значення адреси ❗
        
        Значення не має містити:
        
        ◾ Спеціальні символи ", \\ | / _"
        ◾ Цифри (номер будинку потрібно вводити окремо від вулиці)
        ◾ Англійські або російські літери
        
        Введіть значення ще раз :arrow_down:
        """;

    private final String value;

    public InvalidInputException(String value) {
        super(MESSAGE);
        this.value = value;
    }
}
