package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
@AllArgsConstructor
public class StartHandler implements Handler {

    private static final String START = "/start";

    private final TelegramService telegramService;

    private final UserSessionService userSessionService;

    @Override
    public boolean isHandleable(UserSession userSession) {
        return START.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        //TODO change message and buttons based on user session
        log.info("StartHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        SendMessage sendMessage = SendMessage.builder()
            .text("""
                Привіт! Вас вітає Blackout alers Bot!
                Тут ви можете отримати актуальний графік відключень світла та підписатись на нагадування про відключення.
                
                Для того, щоб почати, натисніть кнопку "Ввести адресу"
                """)
            .chatId(userSession.getChatId())
            .build();

        telegramService.sendMessage(sendMessage);
        userSessionService.saveUserSession(userSession);
    }
}
