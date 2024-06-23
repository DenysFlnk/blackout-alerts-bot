package org.bot.telegram.blackout_alerts.model.session;

import lombok.Data;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;

@Data
public class UserSession {

    private final long chatId;

    private SessionState sessionState = SessionState.START;

    private String text;

    private Address address;

    private Schedule schedule;

    public void setUserCity(String city) {
        address.setCity(city);
    }

    public void setUserStreet(String street) {
        address.setStreet(street);
    }

    public void setUserHouse(String house) {
        address.setHouse(house);
    }

    public void setShutdownGroup(byte shutdownGroup) {
        address.setShutdownGroup(shutdownGroup);
    }

    public String getUserCity() {
        return address != null ? address.getCity() : null;
    }

    public String getUserStreet() {
        return address != null ? address.getStreet() : null;
    }

    public String getUserHouse() {
        return address != null ? address.getHouse() : null;
    }

    public byte getShutdownGroup() {
        return address.getShutdownGroup();
    }
}
