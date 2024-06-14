package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;

@AllArgsConstructor
public abstract class AbstractHandler implements Handler {

    protected final TelegramService telegramService;

    protected final UserSessionService userSessionService;
}
