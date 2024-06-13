package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@AllArgsConstructor
public class StartHandler implements Handler {

    private static final String START = "/start";

    private final TelegramService telegramService;

    @Override
    public boolean isHandleable(UserSession userSession) {
        return START.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        SendMessage sendMessage = SendMessage.builder()
            .text("""
                Привіт! Вас вітає Blackout alers Bot!
                Тут ви можете отримати актуальний графік відключень світла та підписатись на нагадування про відключення.
                
                Для того, щоб почати, натисніть кнопку "Ввести адресу"
                """)
            .chatId(userSession.getChatId())
            .build();

        telegramService.sendMessage(sendMessage);
    }
}
