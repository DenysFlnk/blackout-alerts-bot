package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.ScheduleService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@Slf4j
public class TodayScheduleHandler extends AbstractHandler {

    private static final String TODAY_SCHEDULE = "/today_schedule";

    private final ScheduleService scheduleService;

    public TodayScheduleHandler(TelegramService telegramService, UserSessionService userSessionService,
                                ScheduleService scheduleService) {
        super(telegramService, userSessionService);
        this.scheduleService = scheduleService;
    }

    @Override
    public boolean isHandleable(UserSession userSession) {
        return TODAY_SCHEDULE.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("TodayScheduleHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        if (!SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
            InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
                .addEnterAddressButton()
                .build();

            SendMessage sendMessage = SendMessage.builder()
                .chatId(userSession.getChatId())
                .text(EmojiParser.parseToUnicode(":exclamation: Необхідно ввести повну адресу для отримання графіку відключень"))
                .replyMarkup(keyboard)
                .build();

            telegramService.sendMessage(sendMessage);
            return;
        }

        String schedule;
        try {
            schedule = scheduleService.getRenderedTodaySchedule(userSession);
        } catch (RuntimeException e) {
            log.error("Invalid address.");

            SendMessage sendMessage = SendMessage.builder()
                .chatId(userSession.getChatId())
                .text(e.getMessage())
                .replyMarkup(KeyboardBuilder.builder().addEnterAddressButton().build())
                .build();
            telegramService.sendMessage(sendMessage);
            return;
        }

        SendMessage sendMessage = SendMessage.builder()
            .chatId(userSession.getChatId())
            .parseMode("HTML")
            .text(schedule)
            .build();

        telegramService.sendMessage(sendMessage);
        userSessionService.saveUserSession(userSession);
    }
}
