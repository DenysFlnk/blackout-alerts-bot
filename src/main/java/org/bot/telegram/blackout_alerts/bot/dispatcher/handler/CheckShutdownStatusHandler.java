package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.ShutdownStatusService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class CheckShutdownStatusHandler extends AbstractHandler {

    private static final String CHECK_SHUTDOWN_STATUS = "/check_shutdown_status";

    private final ShutdownStatusService shutdownStatusService;

    public CheckShutdownStatusHandler(TelegramService telegramService, UserSessionService userSessionService,
                                      ShutdownStatusService shutdownStatusService) {
        super(telegramService, userSessionService);
        this.shutdownStatusService = shutdownStatusService;
    }

    @Override
    public boolean isHandleable(UserSession userSession) {
        return CHECK_SHUTDOWN_STATUS.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("Chat id: {}. CheckStatusHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        if (!SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
            log.warn("Chat id: {}. Address not acquired", userSession.getChatId());
            sendAddressNotAcquiredMessage(userSession);
            return;
        }

        sendStatusLoadingMessage(userSession);

        String status = shutdownStatusService.getShutdownStatus(userSession);
        String textMessage = String.format("""
            Статус відключення світла за адресою:
            %s, %s, %s ⬇
            ➖➖➖➖➖➖➖➖➖➖➖➖
            %s
            ➖➖➖➖➖➖➖➖➖➖➖➖
            """, userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse(), status);

        KeyboardBuilder keyboard = KeyboardBuilder.builder()
            .addShowScheduleButton()
            .addShowWeekScheduleButton();

        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(textMessage)
            .replyMarkup(keyboard.build())
            .build();

        telegramService.sendMessage(message);
    }

    private void sendStatusLoadingMessage(UserSession userSession) {
        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text("""
                Перевіряємо статус відключення за вашою адресою 📶
                
                Зазвичай це займає 10-15 секунд 🙏""")
            .build();

        telegramService.sendMessage(message);
    }
}
