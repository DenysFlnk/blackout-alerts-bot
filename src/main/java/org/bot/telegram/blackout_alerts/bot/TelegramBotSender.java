package org.bot.telegram.blackout_alerts.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class TelegramBotSender extends DefaultAbsSender {

    protected TelegramBotSender(@Value("${bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }
}
