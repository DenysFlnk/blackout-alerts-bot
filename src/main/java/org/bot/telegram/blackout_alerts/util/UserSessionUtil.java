package org.bot.telegram.blackout_alerts.util;

public class UserSessionUtil {

    private UserSessionUtil() {
    }

    public static String parseStreet(String street) {
        String[] split = street.split(" ");
        return split.length > 1 ? split[split.length - 1] : street;
    }

    public static String parseHouseNumber(String houseNumber) {
        return houseNumber.replace("\\", "/")
            .replace(",", " ")
            .replaceAll("[a-zA-Z]", " ")
            .split(" ")[0];
    }
}
