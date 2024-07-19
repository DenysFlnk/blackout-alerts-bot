package org.bot.telegram.blackout_alerts.exception;

public class ShutdownStatusUnavailableException extends RuntimeException {

    private static final String MESSAGE = """
        Наразі відсутня інформація щодо статусу відключення по вашій адресі ⛔
        
        Спробуйте пізніше ↪️
        """;

    public ShutdownStatusUnavailableException() {
        super(MESSAGE);
    }
}
