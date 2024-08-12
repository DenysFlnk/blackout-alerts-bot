package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.ScheduleService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class TodayScheduleHandler extends AbstractHandler {

    private static final String TODAY_SCHEDULE_COMMAND = "/today_schedule";

    private static final String HTML = "HTML";

    private final ScheduleService scheduleService;

    public TodayScheduleHandler(TelegramService telegramService, UserSessionService userSessionService,
                                ScheduleService scheduleService) {
        super(telegramService, userSessionService);
        this.scheduleService = scheduleService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return TODAY_SCHEDULE_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (!isAddressAcquired(session)) {
            sendAddressNotAcquiredMessage(session);
            return;
        }

        session.setSessionState(SessionState.TODAY_SCHEDULE);
        sendScheduleLoadingMessage(session);

        String schedule;
        try {
            schedule = scheduleService.getRenderedTodaySchedule(session);
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", session.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(session, e);
            return;
        } finally {
            userSessionService.saveUserSession(session);
        }

        SendMessage sendMessage = SendMessage.builder()
            .chatId(session.getChatId())
            .parseMode(HTML)
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .text(schedule)
            .build();

        telegramService.sendMessage(sendMessage);
    }
}
