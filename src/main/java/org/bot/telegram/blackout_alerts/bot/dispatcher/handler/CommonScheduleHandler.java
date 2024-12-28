package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import static org.bot.telegram.blackout_alerts.util.AddressUtil.KYIV;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.REGION;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
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
public class CommonScheduleHandler extends AbstractHandler {

    private static final String COMMON_KYIV_COMMAND = "/common_kyiv_schedule";
    private static final String COMMON_REGIONS_COMMAND = "/common_regions_schedule";

    private static final String FILE_NAME_FORMAT = "common_schedule_%s";

    private static final String CAPTION_FORMAT = "Загальний графік для %s \uD83D\uDDD3\uFE0F";

    private static byte[] commonKyivSchedule;
    private static LocalDate commonKyivScheduleDate = LocalDate.MIN;

    private static byte[] commonRegionsSchedule;
    private static LocalDate commonRegionsScheduleDate = LocalDate.MIN;

    private final ScheduleService scheduleService;

    public CommonScheduleHandler(TelegramService telegramService, UserSessionService userSessionService,
                                 ScheduleService scheduleService) {
        super(telegramService, userSessionService);
        this.scheduleService = scheduleService;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        String command = session.getText();
        return COMMON_KYIV_COMMAND.equals(command) || COMMON_REGIONS_COMMAND.equals(command);
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        byte[] screenshot;
        if (COMMON_KYIV_COMMAND.equals(session.getText())) {
            if (isNotValid(commonKyivSchedule, commonKyivScheduleDate)) {
                commonKyivSchedule = scheduleService.getCommonScheduleScreenshot(session, KYIV);
                commonKyivScheduleDate = LocalDate.now();
            }

            screenshot = commonKyivSchedule;
        } else {
            if (isNotValid(commonRegionsSchedule, commonRegionsScheduleDate)) {
                commonRegionsSchedule = scheduleService.getCommonScheduleScreenshot(session, REGION);
                commonRegionsScheduleDate = LocalDate.now();
            }

            screenshot = commonRegionsSchedule;
        }

        String fileName = String.format(FILE_NAME_FORMAT, LocalDate.now());
        InputFile file = new InputFile(new ByteArrayInputStream(screenshot), fileName);

        SendPhoto photo = SendPhoto.builder()
            .chatId(session.getChatId())
            .caption(getCaption(session.getText()))
            .photo(file)
            .replyMarkup(KeyboardBuilder.builder().addReturnToMenuButton().build())
            .build();

        telegramService.sendPhoto(photo);
    }

    private static String getCaption(String command) {
        if (COMMON_KYIV_COMMAND.equals(command)) {
            return String.format(CAPTION_FORMAT, "м. Київ");
        } else {
            return String.format(CAPTION_FORMAT, "Київської області");
        }
    }

    private static boolean isNotValid(byte[] screenshot, LocalDate date) {
        return screenshot == null || screenshot.length == 0 || date == null || !date.isEqual(LocalDate.now());
    }
}
