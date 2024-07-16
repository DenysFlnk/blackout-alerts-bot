package org.bot.telegram.blackout_alerts.util;

import java.util.function.Predicate;
import org.bot.telegram.blackout_alerts.exception.InvalidInputException;

public class ValidationUtil {

    private ValidationUtil() {
    }

    public static void validateTextInput(String input) {
        Predicate<String> predicate = isBlank()
            .or(containsForbiddenCharacters())
            .or(containsEnglishLetters())
            .or(containsRussianLetters());

        boolean isInvalid = predicate.test(input);

        if (isInvalid) {
            throw new InvalidInputException(input);
        }
    }

    private static Predicate<String> isBlank() {
        return input -> input == null || input.isBlank();
    }

    private static Predicate<String> containsForbiddenCharacters() {
        return input -> {
            int length = input.length();
            String checked = input.replaceAll("[,\\d\\\\/_|]", "");
            return length != checked.length();
        };
    }

    private static Predicate<String> containsEnglishLetters() {
        return input -> {
            int length = input.length();
            String checked = input.replaceAll("\\w", "");
            return length != checked.length();
        };
    }

    private static Predicate<String> containsRussianLetters() {
        return input -> {
            int length = input.length();
            String checked = input.replace("ы", "");
            return length != checked.length();
        };
    }
}
