package org.bot.telegram.blackout_alerts.exception.address;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidAddressException extends IllegalArgumentException {

    private static final String CITY_ERROR_MESSAGE = """
        Йой! Схоже, що не правильно введений населений пункт 😰
        
        Натисніть "Ввести адресу", щоб спробувати ще 👇
        
        Якщо ви впевнені в правильності введеної адреси  - можливі технічні проблеми з сайтом ДТЕК ⚡
        Спробуйте ваш запит пізніше.
        """;

    private static final String STREET_ERROR_MESSAGE = """
        Йой! Схоже, що не правильно введена вулиця 😰
        
        Натисніть "Ввести адресу", щоб спробувати ще 👇
        
        Якщо ви впевнені в правильності введеної адреси  - спробуйте ввести свою адресу на сайті ДТЕК,
        можливі різні варіанти написання вашої вулиці.
        Після цього спробуйте ще раз.
        """;

    private static final String HOUSE_ERROR_MESSAGE = """
        Йой! Схоже, що не правильно введений номер будинку 😰
        
        Натисніть "Ввести адресу", щоб спробувати ще 👇
        
        Якщо ви впевнені в правильності введеної адреси  - спробуйте ввести свою адресу на сайті ДТЕК,
        можливі різні варіанти написання вашого номеру будинку (2б або 2/б і т.п.).
        Після цього спробуйте ще раз.
        """;

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
