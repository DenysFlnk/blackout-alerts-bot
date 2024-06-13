package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class EnterAddressHandler implements Handler {

    private static final String ENTER_ADDRESS = "/enter_address";

    private final TelegramService telegramService;

    private final Set<SessionState> allowedStates = new HashSet<>();

    public EnterAddressHandler(TelegramService telegramService) {
        this.telegramService = telegramService;

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
        log.info("Current session state: {}, text: {}", userSession.getSessionState(), userSession.getText());
        if (ENTER_ADDRESS.equals(userSession.getText())) {
            log.info("Found /enter_address command");
            telegramService.sendMessage(getEnterCityMessage(userSession));
            userSession.setSessionState(SessionState.WAIT_FOR_CITY);
            return;
        }

        SendMessage message = null;
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
        }

        telegramService.sendMessage(message);
    }

    private SendMessage getEnterCityMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть назву Вашого міста. Наприклад - Київ")
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getEnterStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть назву Вашої вулиці. Наприклад - Хрещатик")
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getEnterHouseMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("Введіть номер Вашого будинку. Наприклад - 23б")
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getAddressAcquiredMessage(UserSession userSession) {
        return SendMessage.builder()
            .text("""
                Адреса успішно збережена!
                Натисніть кнопку "Отримати графік на сьогодні", щоб подивитись графік відключень за Вашою адресою""")
            .chatId(userSession.getChatId())
            .build();
    }
}