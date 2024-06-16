package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
        return SHOW_ADDRESS.equals(userSession.getText()) &&
            SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("ShowAddressHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        String message = String.format("""
            Населенний пункт: %s
            Вулиця: %s
            Будинок: %s
            """, userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse());

        SendMessage sendMessage = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(message)
            .build();

        telegramService.sendMessage(sendMessage);
    }
}
