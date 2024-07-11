package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import com.vdurmont.emoji.EmojiParser;
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

        String message = getAddressMessage(userSession);

        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(message);

        if (SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
            InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
                .addShowScheduleButton()
                .addShowWeekScheduleButton()
                .build();
            messageBuilder.replyMarkup(keyboard);
        }

        telegramService.sendMessage(messageBuilder.build());
    }

    private static String getAddressMessage(UserSession userSession) {
        String noEntry = EmojiParser.parseToUnicode("Не вказано :no_entry_sign:");

        String city = userSession.getUserCity() != null ? userSession.getUserCity() : noEntry;
        String street = userSession.getUserStreet() != null ? userSession.getUserStreet() : noEntry;
        String house = userSession.getUserHouse() != null ? userSession.getUserHouse() : noEntry;

        String message = String.format("""
            :multiple_houses: Населенний пункт :arrow_right: %s
            
            :road: Вулиця :arrow_right: %s
            
            :house: Будинок :arrow_right: %s
            """, city, street, house);

        return EmojiParser.parseToUnicode(message);
    }
}
