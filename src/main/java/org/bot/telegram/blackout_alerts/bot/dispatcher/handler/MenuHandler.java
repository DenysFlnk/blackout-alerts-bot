package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
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
    public boolean isHandleable(UserSession session) {
        return MENU.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        KeyboardBuilder keyboardBuilder = KeyboardBuilder.builder();

        if (isAddressAcquired(session)) {
            keyboardBuilder.addShowAddressButton()
                .addChangeAddressButton()
                .addShowScheduleButton()
                .addShowWeekScheduleButton()
                .addCheckShutdownStatusButton();
        } else {
            keyboardBuilder.addEnterAddressButton();
        }

        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text("⬇ Оберіть варіант з наведених нижче ⬇")
            .replyMarkup(keyboardBuilder.build())
            .build();

        telegramService.sendMessage(message);
    }
}
