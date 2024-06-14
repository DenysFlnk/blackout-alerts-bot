package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.ScheduleService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
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
        return TODAY_SCHEDULE.equals(userSession.getText()) &&
            userSession.getSessionState().equals(SessionState.ADDRESS_ACQUIRED);
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("TodayScheduleHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        String schedule = scheduleService.getRenderedTodaySchedule(userSession);

        SendMessage sendMessage = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(schedule)
            .build();

        telegramService.sendMessage(sendMessage);
    }
}
