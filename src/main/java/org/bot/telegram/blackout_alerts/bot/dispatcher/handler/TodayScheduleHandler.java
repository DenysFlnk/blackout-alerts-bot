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
        log.info("Chat id: {}. TodayScheduleHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        if (!SessionState.ADDRESS_ACQUIRED_STATES.contains(userSession.getSessionState())) {
            log.warn("Chat id: {}. Address not acquired", userSession.getChatId());
            sendAddressNotAcquiredMessage(userSession);
            return;
        }

        userSession.setSessionState(SessionState.TODAY_SCHEDULE);
        sendScheduleLoadingMessage(userSession);

        String schedule;
        try {
            schedule = scheduleService.getRenderedTodaySchedule(userSession);
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", userSession.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(userSession, e);
            return;
        } finally {
            userSessionService.saveUserSession(userSession);
        }

        SendMessage sendMessage = SendMessage.builder()
            .chatId(userSession.getChatId())
            .parseMode("HTML")
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .text(schedule)
            .build();

        telegramService.sendMessage(sendMessage);
    }
}
