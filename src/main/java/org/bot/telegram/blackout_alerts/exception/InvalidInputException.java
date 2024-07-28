package org.bot.telegram.blackout_alerts.exception;

import lombok.Getter;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;

@Getter
public class InvalidInputException extends RuntimeException {

    private static final String CITY_MESSAGE = """
        ❗ Введене не коректне значення населенного пункту ❗
        
        Значення НЕ має містити:
        
        ◾ Тип населенного пункту (місто, село, смт і т.п.)
        ◾ Спеціальні символи
        ◾ Цифри
        ◾ Англійські або російські літери
        
        Введіть значення ще раз ⬇
        """;

    private static final String STREET_MESSAGE = """
        ❗ Введене не коректне значення вулиці ❗
        
        Значення НЕ має містити:
        
        ◾ Спеціальні символи
        ◾ Цифри (номер будинку потрібно вводити окремо від вулиці)
        ◾ Англійські або російські літери
        
        Введіть значення ще раз ⬇
        """;

    private final AddressField addressField;

    private final String value;

    public InvalidInputException(AddressField field, String value) {
        super(getFieldMessage(field));
        this.addressField = field;
        this.value = value;
    }

    private static String getFieldMessage(AddressField field) {
        switch (field) {
            case CITY -> {
                return CITY_MESSAGE;
            }
            case STREET -> {
                return STREET_MESSAGE;
            }
            default -> throw new IllegalArgumentException("Unsupported address field: " + field);
        }
    }
}
