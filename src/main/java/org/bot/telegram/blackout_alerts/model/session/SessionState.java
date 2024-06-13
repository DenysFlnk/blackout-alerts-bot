package org.bot.telegram.blackout_alerts.model.session;

public enum SessionState {
    START,
    WAIT_FOR_CITY,
    WAIT_FOR_STREET,
    WAIT_FOR_HOUSE_NUMBER,
    ADDRESS_ACQUIRED
}
