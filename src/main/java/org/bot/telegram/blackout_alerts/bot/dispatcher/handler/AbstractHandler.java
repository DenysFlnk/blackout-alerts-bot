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

    protected final TelegramService telegramService;

    protected final UserSessionService userSessionService;

    protected void sendAddressNotAcquiredMessage(UserSession userSession) {
        InlineKeyboardMarkup keyboard = KeyboardBuilder.builder()
            .addEnterAddressButton()
            .build();

        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text("❗ Необхідно ввести повну адресу для отримання графіку відключень")
            .replyMarkup(keyboard)
            .build();

        telegramService.sendMessage(message);
    }

    protected void sendScheduleLoadingMessage(UserSession userSession) {
        SendMessage message = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text("""
                Графік завантажується 😎
                
                Зазвичай це займає 10-15 секунд 🙏""")
            .build();

        telegramService.sendMessage(message);
    }

    protected void sendInvalidAddressMessage(UserSession userSession, InvalidAddressException e) {
        SendMessageBuilder messageBuilder = SendMessage.builder()
            .chatId(userSession.getChatId());

        if (e.getAvailableOptions() == null || e.getAvailableOptions().isEmpty()) {
            messageBuilder.text(e.getMessage())
                .replyMarkup(KeyboardBuilder.builder().addEnterAddressButton().build());
        } else {
            log.info("Chat id: {}. Give {} another {} options", userSession.getChatId(),
                e.getAvailableOptions().size(), e.getAddressField());
            String firstLineFromOriginalMessage = e.getMessage().split(System.lineSeparator())[0];
            messageBuilder.text(String.format("""
                %s
                
                Оберіть одну з доступних опцій нижче, щоб спробувати ще 👇
                """, firstLineFromOriginalMessage))
                .replyMarkup(KeyboardBuilder.builder().addAddressOptions(e.getAddressField(), e.getAvailableOptions()).build());
        }

        telegramService.sendMessage(messageBuilder.build());
    }
}
