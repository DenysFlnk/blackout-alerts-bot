package org.bot.telegram.blackout_alerts.model.session;

import lombok.Data;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;

@Data
public class UserSession {

    private final Long chatId;

    private SessionState sessionState = SessionState.START;

    private String text;

    private String userCity;

    private String userStreet;

    private String userHouse;

    private byte shutdownGroup;

    private Schedule schedule;
}
