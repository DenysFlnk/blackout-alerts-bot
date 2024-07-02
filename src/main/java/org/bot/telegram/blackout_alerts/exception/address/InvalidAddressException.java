package org.bot.telegram.blackout_alerts.exception.address;

import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidAddressException extends IllegalArgumentException {

    private static final String CITY_ERROR_MESSAGE = EmojiParser.parseToUnicode("""
                Йой! Схоже, що не правильно введений населений пункт :cold_sweat:
                
                Натисніть "Ввести адресу", щоб спробувати ще :point_down:
                
                Якщо ви впевнені в правильності введеної адреси  - можливі технічні проблеми з сайтом ДТЕК :zap:
                Спробуйте ваш запит пізніше.
                """);

    private static final String STREET_ERROR_MESSAGE = EmojiParser.parseToUnicode("""
                Йой! Схоже, що не правильно введена вулиця :cold_sweat:
                
                Натисніть "Ввести адресу", щоб спробувати ще :point_down:
                
                Якщо ви впевнені в правильності введеної адреси  - можливі технічні проблеми з сайтом ДТЕК :zap:
                Спробуйте ваш запит пізніше.
                """);

    private static final String HOUSE_ERROR_MESSAGE = EmojiParser.parseToUnicode("""
                Йой! Схоже, що не правильно введений номер будинку :cold_sweat:
                
                Натисніть "Ввести адресу", щоб спробувати ще :point_down:
                
                Якщо ви впевнені в правильності введеної адреси  - можливі технічні проблеми з сайтом ДТЕК :zap:
                Спробуйте ваш запит пізніше.
                """);

    private final AddressField addressField;

    private final String fieldValue;

    @Override
    public String getMessage() {
        switch (addressField) {
            case CITY -> {
                return CITY_ERROR_MESSAGE;
            }
            case STREET -> {
                return STREET_ERROR_MESSAGE;
            }
            case HOUSE -> {
                return HOUSE_ERROR_MESSAGE;
            }
            default -> {
                return "Unexpected address field: " + addressField;
            }
        }
    }

    @Override
    public String toString() {
        String s = this.getClass().getName();
        String message = this.getMessage();
        return message != null ? s + ": " + message : s;
    }
}
