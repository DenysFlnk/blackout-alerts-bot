package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChangeAddressHandler extends AbstractHandler {

    private static final String CHANGE_ADDRESS = "/change_address";

    public ChangeAddressHandler(TelegramService telegramService,
                                UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession userSession) {
        return CHANGE_ADDRESS.equals(userSession.getText()) &&
            SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("ChangeAddressHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        userSession.setSessionState(SessionState.WAIT_FOR_CITY);
        telegramService.sendMessage(EnterAddressHandler.getEnterCityMessage(userSession));
        userSessionService.saveUserSession(userSession);
    }
}
