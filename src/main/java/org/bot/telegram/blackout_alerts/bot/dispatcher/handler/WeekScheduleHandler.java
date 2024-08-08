package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.ScheduleService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

@Component
@Slf4j
public class WeekScheduleHandler extends AbstractHandler {

    private static final String WEEK_SCHEDULE = "/week_schedule";

    private final ScheduleService scheduleService;

    public WeekScheduleHandler(TelegramService telegramService, UserSessionService userSessionService,
                               ScheduleService scheduleService) {
        super(telegramService, userSessionService);
        this.scheduleService = scheduleService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return WEEK_SCHEDULE.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (!isAddressAcquired(session)) {
            sendAddressNotAcquiredMessage(session);
            return;
        }

        session.setSessionState(SessionState.WEEK_SCHEDULE);
        sendScheduleLoadingMessage(session);

        ByteArrayInputStream screenshot;
        try {
            screenshot = scheduleService.getWeekScheduleScreenshot(session);
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", session.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(session, e);
            return;
        } finally {
            userSessionService.saveUserSession(session);
        }

        String fileName = String.format("%s_withAddress_%s_%s_%s_date_%s", session.getChatId(),
            session.getUserCity(), session.getUserStreet(), session.getUserHouse(), LocalDateTime.now());
        InputFile file = new InputFile(screenshot, fileName);

        SendPhoto photo = SendPhoto.builder()
            .chatId(session.getChatId())
            .caption(getCaption(session))
            .photo(file)
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .build();

        telegramService.sendPhoto(photo);
    }

    public static String getCaption(UserSession session) {
        return String.format("""
            Графік відключень на тиждень за адресою:
            %s, %s, %s
            
            🚩 Зверніть увагу, що графік актуальний на %s.
            Не забувайте час від часу надсилати запит на тижневий графік повторно, щоб бути впевненим в його актуальності ✅
            """, session.getUserCity(), session.getUserStreet(), session.getUserHouse(),
            LocalDate.now());
    }
}
