package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.AlertSubscription;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.AlertSubscriptionService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class ManageAlertSubscriptionHandler extends AbstractHandler {

    private static final String MANAGE_SUBSCRIPTION_COMMAND = "/manage_subscription";

    private static final String SUB_NOT_FOUND_MESSAGE_FORMAT = """
        Підписок на сповіщення про відключення не знайдено 📵
        
        Створити для поточної адреси?
        %s, %s, %s
        """;
    private static final String VIEW_SUB_MESSAGE_FORMAT = """
        Знайдена активна підписка на сповіщення про відключення 📳
        
        %s, %s, %s
        """;

    private final AlertSubscriptionService alertSubscriptionService;

    public ManageAlertSubscriptionHandler(TelegramService telegramService, UserSessionService userSessionService,
                                          AlertSubscriptionService alertSubscriptionService) {
        super(telegramService, userSessionService);
        this.alertSubscriptionService = alertSubscriptionService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return MANAGE_SUBSCRIPTION_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        Optional<AlertSubscription> subscription = alertSubscriptionService.getAlertSubscription(session.getChatId());

        if (subscription.isEmpty()) {
            sendSubscriptionsNotFoundMessage(session);
        } else {
            sendViewSubscriptionMessage(session, subscription.get());
        }
    }

    private void sendSubscriptionsNotFoundMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(String.format(SUB_NOT_FOUND_MESSAGE_FORMAT, session.getUserCity(), session.getUserStreet(),
                session.getUserHouse()))
            .replyMarkup(KeyboardBuilder.builder().addCreateAlertSubscriptionButton().addReturnToMenuButton().build())
            .build();
        telegramService.sendMessage(message);
    }

    private void sendViewSubscriptionMessage(UserSession session, AlertSubscription subscription) {
        AddressEntity address = subscription.getAddress();
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(String.format(VIEW_SUB_MESSAGE_FORMAT, address.getCity(), address.getStreet(), address.getHouse()))
            .replyMarkup(KeyboardBuilder.builder().addDeleteAlertSubscriptionButton().addReturnToMenuButton().build())
            .build();
        telegramService.sendMessage(message);
    }
}
