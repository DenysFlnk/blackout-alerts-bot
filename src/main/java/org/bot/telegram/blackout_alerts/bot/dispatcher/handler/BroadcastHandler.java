package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.List;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.UserInfoRepository;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class BroadcastHandler extends AbstractHandler {

    private static final String BROADCAST_COMMAND = "/broadcast";

    private static final String PRE_BROADCAST_MESSAGE = "Write message to all users right down below ⬇";

    private final UserInfoRepository userInfoRepository;

    public BroadcastHandler(TelegramService telegramService,
                            UserSessionService userSessionService,
                            UserInfoRepository userInfoRepository) {
        super(telegramService, userSessionService);
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return telegramService.isAdmin(session.getChatId()) && (BROADCAST_COMMAND.equals(session.getText()) ||
            SessionState.START_BROADCAST.equals(session.getSessionState()));
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (BROADCAST_COMMAND.equals(session.getText())) {
            sendPreBroadcastMessage();
            session.setSessionState(SessionState.START_BROADCAST);
            userSessionService.saveUserSession(session);
            return;
        }

        List<Long> users = userInfoRepository.getAllUserIds();

        List<SendMessage> messages = users.stream()
            .map(userId -> SendMessage.builder()
                .chatId(userId)
                .text(session.getText())
                .build())
            .toList();

        telegramService.sendMessages(messages);

        session.setSessionState(SessionState.END_BROADCAST);
        userSessionService.saveUserSession(session);
    }

    private void sendPreBroadcastMessage() {
        SendMessage message = new SendMessage();
        message.setText(PRE_BROADCAST_MESSAGE);
        telegramService.sendMessageToAdmin(message);
    }
}
