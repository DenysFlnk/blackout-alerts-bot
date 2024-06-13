package org.bot.telegram.blackout_alerts.service;

import java.util.HashMap;
import java.util.Map;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

//TODO replace map to actual db storage later
@Component
public class UserSessionService {

    private final Map<Long, UserSession> userSessions = new HashMap<>();

    public UserSession getOrCreateUserSession(Update update) {
        long chatId = update.getMessage().getChatId();
        UserSession session = userSessions.getOrDefault(chatId, new UserSession(chatId));
        session.setText(update.getMessage().getText());
        return session;
    }

    public void saveUserSession(UserSession userSession) {
        userSessions.put(userSession.getChatId(), userSession);
    }
}
