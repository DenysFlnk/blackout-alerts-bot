package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.UTC_PLUS_3;

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

    private static final String WEEK_SCHEDULE_COMMAND = "/week_schedule";

    private static final String FILE_NAME_FORMAT = "%s_withAddress_%s_%s_%s_date_%s";
    private static final String IMAGE_CAPTION_FORMAT = """
        Графік відключень на тиждень за адресою:
        %s, %s, %s
        
        🚩 Зверніть увагу, що графік актуальний на %s.
        Не забувайте час від часу надсилати запит на тижневий графік повторно, щоб бути впевненим в його актуальності ✅
        """;

    private final ScheduleService scheduleService;

    public WeekScheduleHandler(TelegramService telegramService, UserSessionService userSessionService,
                               ScheduleService scheduleService) {
        super(telegramService, userSessionService);
        this.scheduleService = scheduleService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return WEEK_SCHEDULE_COMMAND.equals(session.getText());
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

        String fileName = String.format(FILE_NAME_FORMAT, session.getChatId(),
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
        return String.format(IMAGE_CAPTION_FORMAT, session.getUserCity(), session.getUserStreet(), session.getUserHouse(),
            LocalDate.now(UTC_PLUS_3));
    }
}
