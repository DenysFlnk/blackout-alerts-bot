package org.bot.telegram.blackout_alerts.service;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.bot.TelegramBotSender;
import org.bot.telegram.blackout_alerts.exception.MessageSenderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class TelegramService {

    private final long adminId;

    private final TelegramBotSender sender;

    public TelegramService(@Value("${bot.admin.id}") String adminId, TelegramBotSender sender) {
        this.adminId = Long.parseLong(adminId);
        this.sender = sender;
    }

    public void sendMessage(SendMessage message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            throw new MessageSenderException(e);
        }
    }

    public void sendMessages(List<SendMessage> messages) {
        for (SendMessage message : messages) {
            try {
                sendMessage(message);
            } catch (MessageSenderException e) {
                log.warn("Exception while sending broadcast message for user with id {}. {}", message.getChatId(),
                    e.getMessage());
            }
        }
    }

    public void sendPhoto(SendPhoto message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            throw new MessageSenderException(e);
        }
    }

    public void sendMessageToAdmin(SendMessage message) {
        try {
            message.setChatId(adminId);
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error while sending message {} to admin", message.getText(), e);
        }
    }

    public boolean isAdmin(long chatId) {
        return adminId == chatId;
    }
}
