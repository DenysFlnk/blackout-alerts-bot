package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import com.vdurmont.emoji.EmojiParser;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.bot.telegram.blackout_alerts.util.UserSessionUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
            telegramService.sendMessage(getEnterCityMessage(userSession));

            userSession.setSessionState(SessionState.WAIT_FOR_CITY);
            userSessionService.saveUserSession(userSession);
            return;
        }

        SendMessage message;
        switch (userSession.getSessionState()) {
            case WAIT_FOR_CITY -> {
                log.info("Chat id: {}, entered city: {}", userSession.getChatId(), userSession.getText());
                userSession.setUserCity(userSession.getText());
                message = getEnterStreetMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_STREET);
            }
            case WAIT_FOR_STREET -> {
                log.info("Chat id: {}, entered street: {}", userSession.getChatId(), userSession.getText());
                String street = UserSessionUtil.parseStreet(userSession.getText());
                userSession.setUserStreet(street);
                message = getEnterHouseMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_HOUSE_NUMBER);
            }
            case WAIT_FOR_HOUSE_NUMBER -> {
                log.info("Chat id: {}, entered house: {}", userSession.getChatId(), userSession.getText());
                String house = UserSessionUtil.parseHouseNumber(userSession.getText());
                userSession.setUserHouse(house);
                message = getAddressAcquiredMessage(userSession);
                userSession.setSessionState(SessionState.ADDRESS_ACQUIRED);
                log.info("Chat id: {}, address acquired: {}, {}, {}", userSession.getChatId(), userSession.getUserCity(),
                    userSession.getUserStreet(), userSession.getUserHouse());
            }
            default -> throw new IllegalStateException("Unexpected session state: " + userSession.getSessionState());
        }

        telegramService.sendMessage(message);
        userSessionService.saveUserSession(userSession);
    }

    protected static SendMessage getEnterCityMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode(":point_right: Введіть назву міста, наприклад - Київ"))
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode(":point_right: Введіть назву вулиці, наприклад - Хрещатик"))
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterHouseMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode(":point_right: Введіть номер будинку, наприклад - 23б"))
            .chatId(userSession.getChatId())
            .build();
    }

    private SendMessage getAddressAcquiredMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addShowScheduleButton()
            .addChangeAddressButton()
            .build();

        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode("""
                Адреса успішно збережена :ok_hand:
                Натисніть кнопку "Отримати графік на сьогодні :bulb:", щоб подивитись графік відключень за збереженою адресою або
                "Змінити адресу :arrows_counterclockwise:" для зміни введеної адреси
                """))
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }
}
