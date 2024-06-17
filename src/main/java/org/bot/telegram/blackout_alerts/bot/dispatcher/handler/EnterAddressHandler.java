package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
@Slf4j
public class EnterAddressHandler extends AbstractHandler {

    private static final String ENTER_ADDRESS = "/enter_address";

    private final Set<SessionState> allowedStates = new HashSet<>();

    public EnterAddressHandler(TelegramService telegramService, UserSessionService userSessionService) {
        super(telegramService, userSessionService);

        allowedStates.add(SessionState.WAIT_FOR_CITY);
        allowedStates.add(SessionState.WAIT_FOR_STREET);
        allowedStates.add(SessionState.WAIT_FOR_HOUSE_NUMBER);
    }

    @Override
    public boolean isHandleable(UserSession userSession) {
        return ENTER_ADDRESS.equals(userSession.getText()) || allowedStates.contains(userSession.getSessionState());
    }

    @Override
    public void handle(UserSession userSession) {
        log.info("EnterAddressHandler.handle()");
        log.info("Chat id: {}, session state: {}, text: {}", userSession.getChatId(), userSession.getSessionState(),
            userSession.getText());

        if (ENTER_ADDRESS.equals(userSession.getText())) {
            log.info("Found /enter_address command");
            telegramService.sendMessage(getEnterCityMessage(userSession));
            userSession.setSessionState(SessionState.WAIT_FOR_CITY);
            userSessionService.saveUserSession(userSession);
            return;
        }

        SendMessage message;
        switch (userSession.getSessionState()) {
            case WAIT_FOR_CITY -> {
                userSession.setUserCity(userSession.getText());
                message = getEnterStreetMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_STREET);
            }
            case WAIT_FOR_STREET -> {
                userSession.setUserStreet(userSession.getText());
                message = getEnterHouseMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_HOUSE_NUMBER);
            }
            case WAIT_FOR_HOUSE_NUMBER -> {
                userSession.setUserHouse(userSession.getText());
                message = getAddressAcquiredMessage(userSession);
                userSession.setSessionState(SessionState.ADDRESS_ACQUIRED);
                log.info("Address acquired: {}, {}, {}", userSession.getUserCity(), userSession.getUserStreet(),
                    userSession.getUserHouse());
            }
            default -> throw new IllegalStateException("Unexpected session state: " + userSession.getSessionState());
        }

        telegramService.sendMessage(message);
        userSessionService.saveUserSession(userSession);
    }

    protected static SendMessage getEnterCityMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть назву міста, наприклад - Київ")
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть назву вулиці, наприклад - Хрещатик")
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterHouseMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть номер будинку, наприклад - 23б")
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getAddressAcquiredMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addShowScheduleButton()
            .addChangeAddressButton()
            .build();

        return SendMessage.builder()
            .text("""
                Адреса успішно збережена!
                Натисніть кнопку "Отримати графік на сьогодні", щоб подивитись графік відключень за збереженою адресою або
                "Змінити адресу" для зміни введеної адреси
                """)
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }
}
