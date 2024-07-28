package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@Slf4j
public class ShowAddressHandler extends AbstractHandler {

    private static final String SHOW_ADDRESS_COMMAND = "/show_address";

    private static final String NO_ENTRY = "Не вказано \uD83D\uDEAB";
    private static final String ADDRESS_MESSAGE_FORMAT = """
        🏘 Населенний пункт ➡ %s
        
        🛣 Вулиця ➡ %s
        
        🏚 Будинок ➡ %s
        """;

    public ShowAddressHandler(TelegramService telegramService,
                              UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return SHOW_ADDRESS_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        String message = getAddressMessage(session);

        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(session.getChatId())
            .text(message);

        if (isAddressAcquired(session)) {
            InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
                .addCheckShutdownStatusButton()
                .addShowScheduleButton()
                .addShowWeekScheduleButton()
                .addManageAlertSubscriptionButton()
                .build();
            messageBuilder.replyMarkup(keyboard);
        } else {
            messageBuilder.replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build());
        }

        telegramService.sendMessage(messageBuilder.build());
    }

    private static String getAddressMessage(UserSession session) {
        String city = session.getUserCity() != null ? session.getUserCity() : NO_ENTRY;
        String street = session.getUserStreet() != null ? session.getUserStreet() : NO_ENTRY;
        String house = session.getUserHouse() != null ? session.getUserHouse() : NO_ENTRY;

        return String.format(ADDRESS_MESSAGE_FORMAT, city, street, house);
    }
}
