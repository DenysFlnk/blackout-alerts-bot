package org.bot.telegram.blackout_alerts.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageSenderException extends RuntimeException {

    public MessageSenderException(TelegramApiException e) {
        super(e);
    }
}
