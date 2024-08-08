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

    private static final String SHOW_ADDRESS = "/show_address";

    public ShowAddressHandler(TelegramService telegramService,
                              UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return SHOW_ADDRESS.equals(session.getText());
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
                .build();
            messageBuilder.replyMarkup(keyboard);
        } else {
            messageBuilder.replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build());
        }

        telegramService.sendMessage(messageBuilder.build());
    }

    private static String getAddressMessage(UserSession session) {
        String noEntry = "Не вказано \uD83D\uDEAB";

        String city = session.getUserCity() != null ? session.getUserCity() : noEntry;
        String street = session.getUserStreet() != null ? session.getUserStreet() : noEntry;
        String house = session.getUserHouse() != null ? session.getUserHouse() : noEntry;

        return String.format("""
            🏘 Населенний пункт ➡ %s
            
            🛣 Вулиця ➡ %s
            
            🏚 Будинок ➡ %s
            """, city, street, house);
    }
}
