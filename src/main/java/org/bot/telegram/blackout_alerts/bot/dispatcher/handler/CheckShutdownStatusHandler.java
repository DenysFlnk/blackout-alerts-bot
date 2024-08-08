package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.ShutdownStatusUnavailableException;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
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
    public boolean isHandleable(UserSession session) {
        return CHECK_SHUTDOWN_STATUS.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (!isAddressAcquired(session)) {
            sendAddressNotAcquiredMessage(session);
            return;
        }

        session.setSessionState(SessionState.CHECK_SHUTDOWN_STATUS);
        sendStatusLoadingMessage(session);

        String status;
        try {
            status = shutdownStatusService.getShutdownStatus(session);
        } catch (ShutdownStatusUnavailableException e) {
            SendMessage message = SendMessage.builder()
                .chatId(session.getChatId())
                .text(e.getMessage())
                .build();
            telegramService.sendMessage(message);
            return;
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", session.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(session, e);
            return;
        } finally {
            userSessionService.saveUserSession(session);
        }

        String textMessage = String.format("""
            Статус відключення світла за адресою:
            %s, %s, %s ⬇
            ➖➖➖➖➖➖➖➖➖➖➖➖
            %s
            ➖➖➖➖➖➖➖➖➖➖➖➖
            """, session.getUserCity(), session.getUserStreet(), session.getUserHouse(), status);

        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(textMessage)
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .build();

        telegramService.sendMessage(message);
    }

    private void sendStatusLoadingMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text("""
                Перевіряємо статус відключення за вашою адресою 📶
                
                Зазвичай це займає 10-15 секунд 🙏""")
            .build();

        telegramService.sendMessage(message);
    }
}
