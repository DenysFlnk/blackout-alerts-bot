package org.bot.telegram.blackout_alerts.service;

import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.bot.TelegramBotSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@AllArgsConstructor
public class TelegramService {

    private final TelegramBotSender sender;

    public void sendMessage(SendMessage message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e); //TODO
        }
    }
}
