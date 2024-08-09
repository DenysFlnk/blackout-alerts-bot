package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.Address;
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
    public boolean isHandleable(UserSession session) {
        return CHANGE_ADDRESS.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        session.setAddress(new Address());
        session.setSessionState(SessionState.WAIT_FOR_CITY);

        telegramService.sendMessage(EnterAddressHandler.getEnterCityMessage(session));
        userSessionService.saveUserSession(session);
    }
}
