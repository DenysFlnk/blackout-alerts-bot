package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class DonationHandler extends AbstractHandler {

    private static final String DONATE_COMMAND = "/donate";

    private static final String DONATE_MESSAGE_FORMAT = """
        Бот розробляється однією людиною і витрати на утримання (оплата серверу, підтримка оновлень)
        оплачуються з власних коштів.
        
        Якщо вам сподобався бот, будь-яка сума - це велика підтримка для автора ❤️
        
        Посилання на банку в моно - %s
        
        Або прямим переказом на картку - %s
        """;

    @Value("${bot.donate.jar}")
    private String jarLink;

    @Value("${bot.donate.card}")
    private String cardNumber;

    public DonationHandler(TelegramService telegramService,
                           UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return DONATE_COMMAND.equals(session.getText());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);
        sendDonationMessage(session);
    }

    private void sendDonationMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(String.format(DONATE_MESSAGE_FORMAT, jarLink, cardNumber))
            .build();
        telegramService.sendMessage(message);
    }
}
