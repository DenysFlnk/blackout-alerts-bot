package org.bot.telegram.blackout_alerts.model.entity;

public enum Zone {
    KYIV,
    REGIONS;

    public static Zone findZone(String city) {
        if ("Київ".equals(city)) {
            return KYIV;
        } else {
            return REGIONS;
        }
    }
}
