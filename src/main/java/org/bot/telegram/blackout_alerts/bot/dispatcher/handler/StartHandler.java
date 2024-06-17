package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@Slf4j
public class StartHandler extends AbstractHandler {

    private static final String START = "/start";

    public StartHandler(TelegramService telegramService, UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

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

        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addEnterAddressButton()
            .build();

        SendMessage sendMessage = SendMessage.builder()
            .text("""
                Привіт! Вас вітає Blackout alers Bot!
                Тут ви можете отримати актуальний графік відключень світла та підписатись на нагадування про відключення.
                
                Для того, щоб почати, натисніть кнопку "Ввести адресу"
                """)
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();

        telegramService.sendMessage(sendMessage);
        userSessionService.saveUserSession(userSession);
    }
}
