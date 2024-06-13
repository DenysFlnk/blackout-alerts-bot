package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import org.bot.telegram.blackout_alerts.model.session.UserSession;

public interface Handler {

    boolean isHandleable(UserSession userSession);

    void handle(UserSession userSession);
}
