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
        log.info("Chat id: {}. ShowAddressHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        String message = getAddressMessage(userSession);

        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(message);

        if (SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
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

    private static String getAddressMessage(UserSession userSession) {
        String noEntry = "Не вказано \uD83D\uDEAB";

        String city = userSession.getUserCity() != null ? userSession.getUserCity() : noEntry;
        String street = userSession.getUserStreet() != null ? userSession.getUserStreet() : noEntry;
        String house = userSession.getUserHouse() != null ? userSession.getUserHouse() : noEntry;

        return String.format("""
            🏘 Населенний пункт ➡ %s
            
            🛣 Вулиця ➡ %s
            
            🏚 Будинок ➡ %s
            """, city, street, house);
    }
}
