package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.AlertSubscriptionService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class AlertSubscriptionHandler extends AbstractHandler {

    private static final String ALERT_SUBSCRIPTION_COMMAND = "/alert_subscription";

    private static final String CREATING_SUB_MESSAGE = "Створюємо підписку на сповіщення про відключення світла ⏳";
    private static final String SUB_CREATED_MESSAGE_FORMAT = """
        Підписка на сповіщення про відключення світла створена успішно ✅
        
        Адреса:
        %s, %s, %s
        
        За 15 хв до можливого відключення ви отримаєте сповіщення 🕞
        """;

    private final AlertSubscriptionService alertSubscriptionService;

    public AlertSubscriptionHandler(TelegramService telegramService, UserSessionService userSessionService,
                                    AlertSubscriptionService alertSubscriptionService) {
        super(telegramService, userSessionService);
        this.alertSubscriptionService = alertSubscriptionService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return ALERT_SUBSCRIPTION_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (!isAddressAcquired(session)) {
            sendAddressNotAcquiredMessage(session);
            return;
        }

        session.setSessionState(SessionState.ALERT_SUBSCRIPTION);
        sendSubscribingMessage(session);

        try {
            alertSubscriptionService.subscribeToAlert(session);
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", session.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(session, e);
            return;
        } finally {
            userSessionService.saveUserSession(session);
        }

        sendAlertSubscriptionSuccessMessage(session);
    }

    private void sendSubscribingMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(CREATING_SUB_MESSAGE)
            .build();
        telegramService.sendMessage(message);
    }

    private void sendAlertSubscriptionSuccessMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(String.format(SUB_CREATED_MESSAGE_FORMAT, session.getUserCity(), session.getUserStreet(),
                session.getUserHouse()))
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .build();
        telegramService.sendMessage(message);
    }
}
