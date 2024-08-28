package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import static org.bot.telegram.blackout_alerts.util.AddressUtil.isKyiv;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.parseHouseNumber;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.parseKyivStreetPrefix;
import static org.bot.telegram.blackout_alerts.util.ValidationUtil.validateCityInput;
import static org.bot.telegram.blackout_alerts.util.ValidationUtil.validateHouseInput;
import static org.bot.telegram.blackout_alerts.util.ValidationUtil.validateStreetInput;

import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@Slf4j
public class EnterAddressHandler extends AbstractHandler {

    private static final String ENTER_ADDRESS_COMMAND = "/enter_address";

    private static final String ENTER_CITY_MESSAGE = "👉 Введіть назву міста, наприклад - Київ";
    private static final String ENTER_KYIV_STREET_MESSAGE = """
        👉 Введіть назву вулиці, наприклад - вулиця Хрещатик
        
        Якщо це площа, проспект або бульвар - додайте на початок площа, проспект або бульвар замість вулиці відповідно
        """;
    private static final String ENTER_REGION_STREET_MESSAGE = "👉 Введіть назву вулиці, наприклад - Соборна ";
    private static final String ENTER_HOUSE_MESSAGE = "👉 Введіть номер будинку, наприклад - 2б";
    private static final String ADDRESS_ACQUIRED_MESSAGE = """
        Адреса успішно збережена \uD83D\uDC4C
        
        ⬇ Доступні функції ⬇
        """;

    public EnterAddressHandler(TelegramService telegramService, UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return ENTER_ADDRESS_COMMAND.equals(session.getText()) ||
               SessionState.WAIT_FOR_INPUTS.contains(session.getSessionState());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (ENTER_ADDRESS_COMMAND.equals(session.getText())) {
            telegramService.sendMessage(getEnterCityMessage(session));

            session.setSessionState(SessionState.WAIT_FOR_CITY);
            userSessionService.saveUserSession(session);
            return;
        }

        SendMessage message;
        switch (session.getSessionState()) {
            case WAIT_FOR_CITY -> {
                log.info("Chat id: {}. Entered city: {}", session.getChatId(), session.getText());
                String city = session.getText();
                validateCityInput(city);
                session.setUserCity(city);
                message = isKyiv(city) ? getEnterKyivStreetMessage(session) : getEnterRegionStreetMessage(session);
                session.setSessionState(SessionState.WAIT_FOR_STREET);
            }
            case WAIT_FOR_STREET -> {
                log.info("Chat id: {}. Entered street: {}", session.getChatId(), session.getText());
                String text = session.getText();
                validateStreetInput(text);
                String street = isKyiv(session.getUserCity()) ? parseKyivStreetPrefix(text) : text;
                session.setUserStreet(street);
                message = getEnterHouseMessage(session);
                session.setSessionState(SessionState.WAIT_FOR_HOUSE_NUMBER);
            }
            case WAIT_FOR_HOUSE_NUMBER -> {
                log.info("Chat id: {}. Entered house: {}", session.getChatId(), session.getText());
                String text = session.getText();
                validateHouseInput(text);
                String house = parseHouseNumber(text);
                session.setUserHouse(house);
                message = getAddressAcquiredMessage(session);
                session.setSessionState(SessionState.ADDRESS_ACQUIRED);
                log.info("Chat id: {}. Address acquired: {}, {}, {}", session.getChatId(), session.getUserCity(),
                    session.getUserStreet(), session.getUserHouse());
            }
            default -> throw new IllegalStateException("Unexpected session state: " + session.getSessionState());
        }

        telegramService.sendMessage(message);
        userSessionService.saveUserSession(session);
    }

    protected static SendMessage getEnterCityMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(ENTER_CITY_MESSAGE)
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterKyivStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(ENTER_KYIV_STREET_MESSAGE)
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterRegionStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(ENTER_REGION_STREET_MESSAGE)
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterHouseMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(ENTER_HOUSE_MESSAGE)
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getAddressAcquiredMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addCheckShutdownStatusButton()
            .addShowScheduleButton()
            .addShowWeekScheduleButton()
            .addChangeAddressButton()
            .build();

        return SendMessage.builder()
            .text(ADDRESS_ACQUIRED_MESSAGE)
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }
}
