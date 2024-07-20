package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@AllArgsConstructor
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

    protected void sendInvalidAddressMessage(UserSession userSession, String message) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(userSession.getChatId())
            .text(message)
            .replyMarkup(KeyboardBuilder.builder().addEnterAddressButton().build())
            .build();
        telegramService.sendMessage(sendMessage);
    }
}
