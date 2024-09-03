package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@AllArgsConstructor
@Slf4j
public abstract class AbstractHandler implements Handler {

    private static final String ADDRESS_NOT_ACQUIRED_MESSAGE =
        "❗ Необхідно ввести повну адресу для отримання графіку відключень";
    private static final String SCHEDULE_LOADING_MESSAGE = """
        Графік завантажується 😎
        
        Зазвичай це займає 10-15 секунд 🙏""";
    private static final String CHOOSE_OPTION_MESSAGE_FORMAT = """
        %s
        
        Оберіть одну з доступних опцій нижче, щоб спробувати ще 👇
        """;

    protected final TelegramService telegramService;

    protected final UserSessionService userSessionService;

    protected void logStartHandle(UserSession session) {
        long chatId = session.getChatId();
        log.info("Chat id: {}. {}.handle()", chatId, this.getClass().getSimpleName());
        log.info("Chat id: {}. Session state: {}. Text: {}", chatId, session.getSessionState(),
            session.getText());
    }

    protected boolean isAddressAcquired(UserSession session) {
        return !(session.getUserCity() == null || session.getUserStreet() == null || session.getUserHouse() == null);
    }

    protected void sendAddressNotAcquiredMessage(UserSession session) {
        log.warn("Chat id: {}. Address not acquired", session.getChatId());

        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addEnterAddressButton()
            .build();

        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(ADDRESS_NOT_ACQUIRED_MESSAGE)
            .replyMarkup(keyboard)
            .build();

        telegramService.sendMessage(message);
    }

    protected void sendScheduleLoadingMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(SCHEDULE_LOADING_MESSAGE)
            .build();

        telegramService.sendMessage(message);
    }

    protected void sendInvalidAddressMessage(UserSession session, InvalidAddressException e) {
        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(session.getChatId());

        if (e.getAvailableOptions() == null || e.getAvailableOptions().isEmpty()) {
            messageBuilder.text(e.getMessage())
                .replyMarkup(KeyboardBuilder.builder().addShowAddressButton().addEnterAddressButton().build());
        } else {
            log.info("Chat id: {}. Give {} another {} options", session.getChatId(),
                e.getAvailableOptions().size(), e.getAddressField());
            String firstLineFromOriginalMessage = e.getMessage().split(System.lineSeparator())[0];
            messageBuilder.text(String.format(CHOOSE_OPTION_MESSAGE_FORMAT, firstLineFromOriginalMessage))
                .replyMarkup(KeyboardBuilder.builder().addAddressOptions(e.getAddressField(), e.getAvailableOptions()).build());
        }

        telegramService.sendMessage(messageBuilder.build());
    }
}
