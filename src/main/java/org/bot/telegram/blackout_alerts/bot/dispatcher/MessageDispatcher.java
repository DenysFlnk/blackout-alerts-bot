package org.bot.telegram.blackout_alerts.bot.dispatcher;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.bot.dispatcher.handler.Handler;
import org.bot.telegram.blackout_alerts.exception.InvalidInputException;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
@AllArgsConstructor
public class MessageDispatcher {

    private final List<Handler> handlers;

    private final TelegramService telegramService;

    public void dispatch(UserSession session) {
        try {
            handlers.stream()
                .filter(handler -> handler.isHandleable(session))
                .findAny()
                .ifPresent(handler -> handler.handle(session));
        } catch (InvalidInputException e) {
            log.error("Chat id: {}. Entered invalid value {}", session.getChatId(), e.getValue());
            SendMessage message = SendMessage.builder()
                .chatId(session.getChatId())
                .text(e.getMessage())
                .build();
            telegramService.sendMessage(message);
        } catch (Exception e) {
            log.error("Chat id: {}. Exception while dispatching message", session.getChatId(), e);
            SendMessage message = SendMessage.builder()
                .chatId(session.getChatId())
                .text("""
                     Схоже, що сталася помилка при обробці вашого запиту  😥
                    
                     Спробуйте, будь ласка, пізніше 🕛
                    """)
                .build();
            telegramService.sendMessage(message);
        }
    }
}
