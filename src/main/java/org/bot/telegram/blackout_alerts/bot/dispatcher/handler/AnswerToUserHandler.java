package org.bot.telegram.blackout_alerts.bot.dispatcher.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bot.telegram.blackout_alerts.exception.TextParsingException;
import org.bot.telegram.blackout_alerts.model.session.SessionState;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class AnswerToUserHandler extends AbstractHandler {

    private static final String ANSWER_TO_USER_COMMAND = "/answer_to_user";

    private static final String PRE_ANSWER_MESSAGE = "Copy message from user and fill Answer section ⬇️";
    private static final String ANSWER_MESSAGE_FORMAT = """
        Привіт! Ви надсилали питання ⬇️
        "%s"
        
        Відповідь ⬇️
        "%s"
        
        Якщо у вас залишились ще питання - оберіть "Задати питання 🙋" із головного меню
        """;

    private static final Pattern CHAT_ID_PATTERN = Pattern.compile("From:\\s?(\\d*)");
    private static final Pattern QUESTION_PATTERN = Pattern.compile("Question:\\s?(.*)Answer");
    private static final Pattern ANSWER_PATTERN = Pattern.compile("Answer:\\s?(.*)$");

    public AnswerToUserHandler(TelegramService telegramService,
                               UserSessionService userSessionService) {
        super(telegramService, userSessionService);
    }

    @Override
    public boolean isHandleable(UserSession session) {
        return ANSWER_TO_USER_COMMAND.equals(session.getText()) ||
            SessionState.ANSWER_TO_USER.equals(session.getSessionState());
    }

    @Override
    public void handle(UserSession session) {
        logStartHandle(session);

        if (ANSWER_TO_USER_COMMAND.equals(session.getText())) {
            sendAnswerFormatMessage();
            session.setSessionState(SessionState.ANSWER_TO_USER);
            userSessionService.saveUserSession(session);
            return;
        }

        String text = session.getText().replace(System.lineSeparator(), " ");

        SendMessage answerToUser = SendMessage.builder()
            .chatId(parseChatId(text))
            .text(String.format(ANSWER_MESSAGE_FORMAT, parseQuestion(text), parseAnswer(text)))
            .build();
        telegramService.sendMessage(answerToUser);

        session.setSessionState(SessionState.QUESTION_ANSWERED);
        userSessionService.saveUserSession(session);
    }

    private static long parseChatId(String text) {
        Matcher matcher = CHAT_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        } else {
            throw new TextParsingException("Failed to parse chat id");
        }
    }

    private static String parseQuestion(String text) {
        Matcher matcher = QUESTION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new TextParsingException("Failed to parse question");
        }
    }

    private static String parseAnswer(String text) {
        Matcher matcher = ANSWER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new TextParsingException("Failed to parse answer");
        }
    }

    private void sendAnswerFormatMessage() {
        SendMessage answer = new SendMessage();
        answer.setText(PRE_ANSWER_MESSAGE);
        telegramService.sendMessageToAdmin(answer);
    }
}
