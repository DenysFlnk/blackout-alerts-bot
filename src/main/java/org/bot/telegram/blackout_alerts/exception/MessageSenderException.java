package org.bot.telegram.blackout_alerts.exception;

import lombok.Getter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Getter
public class MessageSenderException extends RuntimeException {

    private final TelegramApiException cause;

    public MessageSenderException(TelegramApiException e) {
        super(e);
        this.cause = e;
    }
}
