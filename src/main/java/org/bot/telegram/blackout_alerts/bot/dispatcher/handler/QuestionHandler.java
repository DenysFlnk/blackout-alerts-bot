package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.bot.telegram.blackout_alerts.util.KeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class QuestionHandler extends AbstractHandler {

    private static final String ASK_QUESTION_COMMAND = "/ask_question";

    private static final String ENTER_QUESTION = "Напишіть ваше запитання ⬇️";
    private static final String QUESTION_ACCEPTED = "Дякуємо за звернення, очікуйте відповідь найближчим часом! ✅";
    private static final String QUESTION_FORMAT = """
        RECEIVED QUESTION FROM USER
        
        From: %s
        
        Question: %s
        
        Answer:
        """;

    public QuestionHandler(TelegramService telegramService,
                           UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return ASK_QUESTION_COMMAND.equals(session.getText()) ||
            SessionState.WAIT_FOR_QUESTION.equals(session.getSessionState());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (ASK_QUESTION_COMMAND.equals(session.getText())) {
            sendWaitForQuestionMessage(session);
            session.setSessionState(SessionState.WAIT_FOR_QUESTION);
            userSessionService.saveUserSession(session);
            return;
        }

        try {
            sendQuestionAcceptedMessage(session);
        } finally {
            session.setSessionState(SessionState.QUESTION_ASKED);
            userSessionService.saveUserSession(session);
        }

        sendQuestionToAdmin(session);
    }

    private void sendWaitForQuestionMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(ENTER_QUESTION)
            .build();
        telegramService.sendMessage(message);
    }

    private void sendQuestionAcceptedMessage(UserSession session) {
        SendMessage message = SendMessage.builder()
            .chatId(session.getChatId())
            .text(QUESTION_ACCEPTED)
            .build();
        telegramService.sendMessage(message);
    }

    private void sendQuestionToAdmin(UserSession session) {
        SendMessage question = new SendMessage();
        question.setText(String.format(QUESTION_FORMAT, session.getChatId(), session.getText()));
        question.setReplyMarkup(KeyboardBuilder.builder().addAnswerToUserButton().build());
        telegramService.sendMessageToAdmin(question);
    }
}
