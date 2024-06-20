package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
        log.info("StartHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        InlineKeyboardMarkup menu = KeyboardBuilder.builder()
            .addShowAddressButton()
            .addEnterAddressButton()
            .addShowScheduleButton()
            .build();

        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(EmojiParser.parseToUnicode("Оберіть варіант з наведених нижче :arrow_down:"))
            .replyMarkup(menu)
            .build();

        telegramService.sendMessage(message);
    }
}
