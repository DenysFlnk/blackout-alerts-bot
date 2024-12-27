package org.bot.telegram.blackout_alerts.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddressUtil {

    public static final String KYIV = "Київ";
    public static final String REGION = "Область";


    private static final Map<String, String> streetPrefixMap = new HashMap<>();

    private static final String STR = "вул.";

    static {
        streetPrefixMap.put("вулиця", STR);
        streetPrefixMap.put("площа", "пл.");
        streetPrefixMap.put("проспект", "просп.");
        streetPrefixMap.put("бульвар", "бульв.");
        streetPrefixMap.put("провулок", "пров.");
    }

    private static final Set<String> streetPrefixSet = new HashSet<>(streetPrefixMap.values());

    private AddressUtil() {
    }

    public static boolean isKyiv(String city) {
        return KYIV.equals(city);
    }

    public static String parseLastPartOfStreet(String street) {
        String[] split = street.split(" ");
        return split.length > 1 ? split[split.length - 1] : street;
    }

    public static String parseKyivStreetPrefix(String street) {
        String[] split = street.split(" ");

        if (streetPrefixSet.contains(split[0])) {
            return street;
        }

        String prefix = streetPrefixMap.getOrDefault(split[0].toLowerCase(), STR);

        StringBuilder result = new StringBuilder(prefix);
        int idx = split.length > 1 ? 1 : 0;
        for (; idx < split.length; idx++) {
            result.append(" ");
            result.append(split[idx]);
        }

        return result.toString();
    }

    public static String parseHouseNumber(String houseNumber) {
        return houseNumber.replace("\\", "/")
            .replace(",", " ")
            .replaceAll("[a-zA-Z]", " ")
            .split(" ")[0];
    }
}
