package org.bot.telegram.blackout_alerts.exception;

import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {

    private static final String MESSAGE = EmojiParser.parseToUnicode("""
        :exclamation: Введене не коректне значення адреси :exclamation:
        
        Значення не має містити:
        
        :black_medium_small_square: Спеціальні символи ", \\ | / _"
        :black_medium_small_square: Цифри (номер будинку потрібно вводити окремо від вулиці)
        :black_medium_small_square: Англійські або російські літери
        
        Введіть значення ще раз :arrow_down:
        """);

    private final String value;

    public InvalidInputException(String value) {
        super(MESSAGE);
        this.value = value;
    }
}
