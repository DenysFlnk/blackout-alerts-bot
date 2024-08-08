package org.bot.telegram.blackout_alerts.service;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.bot.TelegramBot;
import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.UserInfoRepository;
import org.bot.telegram.blackout_alerts.util.UserSessionUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class UserSessionService {

    private final UserInfoRepository userInfoRepository;

    public UserSession getOrCreateUserSession(Update update) {
        long chatId;
        String newText;
        if (TelegramBot.hasMessageText(update)) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            newText = message.getText();
        } else {
            CallbackQuery callback = update.getCallbackQuery();
            chatId = callback.getMessage().getChatId();
            newText = callback.getData();
        }

        UserSession session = getFromDb(chatId).orElse(new UserSession(chatId));
        saveUserSession(session);
        session.setText(newText);

        return session;
    }

    public void saveUserSession(UserSession session) {
        userInfoRepository.save(UserSessionUtil.getUserInfo(session));
    }

    private Optional<UserSession> getFromDb(long chatId) {
        Optional<UserInfo> userInfo = userInfoRepository.findById(chatId);
        return userInfo.map(UserSessionUtil::getUserSession);
    }
}
