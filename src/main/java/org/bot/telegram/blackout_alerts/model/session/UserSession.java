package org.bot.telegram.blackout_alerts.model.session;

import lombok.Data;

@Data
public class UserSession {

    private final Long chatId;

    private SessionState sessionState = SessionState.START;

    private String text;

    private String userCity;

    private String userStreet;

    private String userHouse;
}
