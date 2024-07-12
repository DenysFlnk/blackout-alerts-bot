package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import com.vdurmont.emoji.EmojiParser;
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
    public boolean isHandleable(UserSession userSession) {
        return WEEK_SCHEDULE.equals(userSession.getText());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("Chat id: {}. WeekScheduleHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        if (!SessionState.ADDRESS_ACQUIRED.equals(userSession.getSessionState())) {
            log.warn("Chat id: {}. Address not acquired", userSession.getChatId());
            sendAddressNotAcquiredMessage(userSession);
            return;
        }

        sendScheduleLoadingMessage(userSession);

        ByteArrayInputStream screenshot;
        try {
            screenshot = scheduleService.getWeekScheduleScreenshot(userSession);
        } catch (InvalidAddressException e) {
            log.error("Chat id: {}. Invalid address for field {}, value {}", userSession.getChatId(),
                e.getAddressField(), e.getFieldValue());
            sendInvalidAddressMessage(userSession, e.getMessage());
            return;
        }

        String fileName = String.format("%s_withAddress_%s_%s_%s_date_%s", userSession.getChatId(),
            userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse(), LocalDateTime.now());
        InputFile file = new InputFile(screenshot, fileName);

        SendPhoto photo = SendPhoto.builder()
            .chatId(userSession.getChatId())
            .caption(getCaption(userSession))
            .photo(file)
            .build();

        telegramService.sendPhoto(photo);
    }

    public static String getCaption(UserSession userSession) {
        return String.format(EmojiParser.parseToUnicode("""
            Графік відключень на тиждень за адресою:
            %s, %s, %s
            
            :triangular_flag_on_post: Зверніть увагу, що графік актуальний на %s.
            Не забувайте час від часу надсилати запит на тижневий графік повторно, щоб бути впевненим в його актуальності :white_check_mark:
            """), userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse(),
            LocalDate.now());
    }
}
