package org.bot.telegram.blackout_alerts.bot.dispatcher;

import java.util.List;
import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.bot.dispatcher.handler.Handler;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MessageDispatcher {

    private final List<Handler> handlers;

    public void dispatch(UserSession session) {
        handlers.stream()
            .filter(handler -> handler.isHandleable(session))
            .findAny()
            .ifPresent(handler -> handler.handle(session));
    }
}
