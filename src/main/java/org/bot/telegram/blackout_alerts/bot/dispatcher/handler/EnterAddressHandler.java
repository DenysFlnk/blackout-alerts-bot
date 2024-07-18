package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import static org.bot.telegram.blackout_alerts.util.AddressUtil.isKyiv;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.parseHouseNumber;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.parseKyivStreetPrefix;
import static org.bot.telegram.blackout_alerts.util.ValidationUtil.validateTextInput;

import com.vdurmont.emoji.EmojiParser;
import java.util.HashSet;
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
        log.info("Chat id: {}. EnterAddressHandler.handle()", userSession.getChatId());
        log.info("Chat id: {}. Session state: {}. Text: {}", userSession.getChatId(), userSession.getSessionState(),
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
                log.info("Chat id: {}. Entered city: {}", userSession.getChatId(), userSession.getText());
                String city = userSession.getText();
                validateTextInput(city);
                userSession.setUserCity(city);
                message = isKyiv(city) ? getEnterKyivStreetMessage(userSession) : getEnterRegionStreetMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_STREET);
            }
            case WAIT_FOR_STREET -> {
                log.info("Chat id: {}. Entered street: {}", userSession.getChatId(), userSession.getText());
                String text = userSession.getText();
                validateTextInput(text);
                String street = isKyiv(userSession.getUserCity()) ? parseKyivStreetPrefix(text) : text;
                userSession.setUserStreet(street);
                message = getEnterHouseMessage(userSession);
                userSession.setSessionState(SessionState.WAIT_FOR_HOUSE_NUMBER);
            }
            case WAIT_FOR_HOUSE_NUMBER -> {
                log.info("Chat id: {}. Entered house: {}", userSession.getChatId(), userSession.getText());
                String house = parseHouseNumber(userSession.getText()).toUpperCase();
                userSession.setUserHouse(house);
                message = getAddressAcquiredMessage(userSession);
                userSession.setSessionState(SessionState.ADDRESS_ACQUIRED);
                log.info("Chat id: {}. Address acquired: {}, {}, {}", userSession.getChatId(), userSession.getUserCity(),
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

    private static SendMessage getEnterKyivStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode("""
                :point_right: Введіть назву вулиці, наприклад - вулиця Хрещатик
                
                Якщо це площа, проспект або бульвар - додайте на початок площа, проспект або бульвар замість вулиці відповідно
                """))
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterRegionStreetMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode("""
                :point_right: Введіть назву вулиці, наприклад - Соборна
                """))
            .chatId(userSession.getChatId())
            .build();
    }

    private static SendMessage getEnterHouseMessage(UserSession userSession) {
        return SendMessage.builder()
            .text(EmojiParser.parseToUnicode(":point_right: Введіть номер будинку, наприклад - 2б"))
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
            .text(EmojiParser.parseToUnicode("Адреса успішно збережена :ok_hand:"))
            .chatId(userSession.getChatId())
            .replyMarkup(keyboard)
            .build();
    }
}
