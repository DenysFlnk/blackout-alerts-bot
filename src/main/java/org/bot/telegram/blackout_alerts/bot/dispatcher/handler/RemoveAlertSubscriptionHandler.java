package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.AlertSubscriptionService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class RemoveAlertSubscriptionHandler extends AbstractHandler {

    private static final String REMOVE_ALERT_SUBSCRIPTION_COMMAND = "/remove_alert_subscription";

    private static final String SUB_CANCELLED_MESSAGE = "Підписку на сповіщення скасовано \uD83D\uDCEA";

    private final AlertSubscriptionService alertSubscriptionService;

    public RemoveAlertSubscriptionHandler(TelegramService telegramService, UserSessionService userSessionService,
                                          AlertSubscriptionService alertSubscriptionService) {
        super(telegramService, userSessionService);
        this.alertSubscriptionService = alertSubscriptionService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return REMOVE_ALERT_SUBSCRIPTION_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);
        alertSubscriptionService.unsubscribeFromAlert(session.getChatId());
        sendUnsubscribedMessage(session);
    }

    private void sendUnsubscribedMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(SUB_CANCELLED_MESSAGE)
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .build();
        telegramService.sendMessage(message);
    }
}
