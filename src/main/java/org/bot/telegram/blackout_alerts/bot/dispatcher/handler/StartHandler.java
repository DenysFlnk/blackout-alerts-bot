package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
        log.info("Chat id: {}. StartHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        SendMessage sendMessage;
        if (SessionState.START.equals(userSession.getSessionState())) {
            sendMessage = getWelcomeMessage(userSession);
        } else {
            sendMessage = getWelcomeBackMessage(userSession);
        }

        telegramService.sendMessage(sendMessage);
    }

    private static SendMessage getWelcomeBackMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addShowAddressButton()
            .addChangeAddressButton()
            .addShowScheduleButton()
            .build();

        return SendMessage.builder()
            .text("""
                Привіт! З поверненням! 🇺🇦
                
                Оберіть варіант з наведених нижче ⬇
                """)
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }

    private static SendMessage getWelcomeMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addEnterAddressButton()
            .build();

        return SendMessage.builder()
            .text("""
                Привіт! 🇺🇦
                
                Тут ви можете отримати актуальний графік відключень світла та підписатись на нагадування про відключення 💡
                
                Для того, щоб почати, натисніть кнопку "Ввести адресу 🏘"
                """)
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }
}
