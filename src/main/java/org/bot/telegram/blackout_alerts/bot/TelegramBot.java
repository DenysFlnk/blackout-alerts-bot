package org.bot.telegram.blackout_alerts.bot;

import org.bot.telegram.blackout_alerts.bot.dispatcher.MessageDispatcher;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component(value = "telegramBot")
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    private final MessageDispatcher dispatcher;

    private final UserSessionService userSessionService;

    public TelegramBot(@Value("${bot.token}") String botToken, MessageDispatcher dispatcher,
                       UserSessionService userSessionService) {
        super(botToken);
        this.dispatcher = dispatcher;
        this.userSessionService = userSessionService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            UserSession userSession = userSessionService.getOrCreateUserSession(update);
            dispatcher.dispatch(userSession);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
