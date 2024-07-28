package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class MenuHandler extends AbstractHandler {

    private static final String MENU = "/menu";

    public MenuHandler(TelegramService telegramService, UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession userSession) {
        return MENU.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("Chat id: {}. MenuHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        KeyboardBuilder keyboardBuilder = KeyboardBuilder.builder();

        if (SessionState.ADDRESS_ACQUIRED_STATES.contains(userSession.getSessionState())) {
            keyboardBuilder.addShowAddressButton()
                .addChangeAddressButton()
                .addShowScheduleButton()
                .addShowWeekScheduleButton()
                .addCheckShutdownStatusButton();
        } else {
            keyboardBuilder.addEnterAddressButton();
        }

        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text("⬇ Оберіть варіант з наведених нижче ⬇")
            .replyMarkup(keyboardBuilder.build())
            .build();

        telegramService.sendMessage(message);
    }
}
