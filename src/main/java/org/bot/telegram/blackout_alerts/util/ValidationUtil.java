package org.bot.telegram.blackout_alerts.util;

import java.util.function.Predicate;
import org.bot.telegram.blackout_alerts.exception.InvalidInputException;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;

public class ValidationUtil {

    private static final String EMPTY = "";

    private ValidationUtil() {
    }

    public static void validateCityInput(String city) {
        Predicate<String> predicate = containsDot()
            .or(containsCityType())
            .or(isBlank())
            .or(containsForbiddenCharacters())
            .or(containsEnglishLetters())
            .or(containsRussianLetters());

        boolean isInvalid = predicate.test(city);

        if (isInvalid) {
            throw new InvalidInputException(AddressField.CITY, city);
        }
    }

    public static void validateStreetInput(String street) {
        Predicate<String> predicate = isBlank()
            .or(containsForbiddenCharacters())
            .or(containsEnglishLetters())
            .or(containsRussianLetters());

        boolean isInvalid = predicate.test(street);

        if (isInvalid) {
            throw new InvalidInputException(AddressField.STREET, street);
        }
    }

    public static void validateHouseInput(String street) {
        Predicate<String> predicate = notContainsDigit();

        boolean isInvalid = predicate.test(street);

        if (isInvalid) {
            throw new InvalidInputException(AddressField.STREET, street);
        }
    }

    private static Predicate<String> isBlank() {
        return input -> input == null || input.isBlank();
    }

    private static Predicate<String> containsForbiddenCharacters() {
        return input -> {
            int length = input.length();
            String checked = input.replaceAll("[,\\d\\\\/_|]", EMPTY);
            return length != checked.length();
        };
    }

    private static Predicate<String> containsEnglishLetters() {
        return input -> {
            int length = input.length();
            String checked = input.replaceAll("\\w", EMPTY);
            return length != checked.length();
        };
    }

    private static Predicate<String> containsRussianLetters() {
        return input -> {
            int length = input.length();
            String checked = input.replace("ы", EMPTY);
            return length != checked.length();
        };
    }

    private static Predicate<String> containsCityType() {
        return input -> {
            int length = input.length();
            String checked = input.toLowerCase().replaceAll("(село|смт|місто|район|область)", EMPTY);
            return length != checked.length();
        };
    }

    private static Predicate<String> containsDot() {
        return input ->  input.contains(".");
    }

    private static Predicate<String> notContainsDigit() {
        return input -> {
            int length = input.length();
            String checked = input.replaceAll("\\d", EMPTY);
            return length == checked.length();
        };
    }
}
