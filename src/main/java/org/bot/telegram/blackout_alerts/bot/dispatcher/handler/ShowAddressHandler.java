package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
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
    public boolean isHandleable(UserSession userSession) {
        return SHOW_ADDRESS.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("ShowAddressHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        String city = userSession.getUserCity() != null ? userSession.getUserCity() : "Не вказано";
        String street = userSession.getUserStreet() != null ? userSession.getUserStreet() : "Не вказано";
        String house = userSession.getUserHouse() != null ? userSession.getUserHouse() : "Не вказано";

        String message = String.format("""
            Населенний пункт: %s
            Вулиця: %s
            Будинок: %s
            """, city, street, house);

        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(message);

        if (SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
            InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
                .addShowScheduleButton()
                .build();
            messageBuilder.replyMarkup(keyboard);
        }

        telegramService.sendMessage(messageBuilder.build());
    }
}
