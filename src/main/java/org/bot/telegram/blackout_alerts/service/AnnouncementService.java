package org.bot.telegram.blackout_alerts.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.MessageSenderException;
import org.bot.telegram.blackout_alerts.model.entity.Announcement;
import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.bot.telegram.blackout_alerts.repository.AnnouncementRepository;
import org.bot.telegram.blackout_alerts.repository.UserInfoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
@AllArgsConstructor
public class AnnouncementService implements CommandLineRunner {

    private static final String START_MESSAGE = """
        Привіт! Оновлення бота вже тут :arrows_clockwise:
        
        Що змінилось:
        
        """;

    private final TelegramService telegramService;

    private final AnnouncementRepository announcementRepository;

    private final UserInfoRepository userInfoRepository;

    @Override
    public void run(String... args) {
        List<Announcement> announcements = announcementRepository.findAllNotAnnounced();

        if (announcements.isEmpty()) {
            return;
        }

        StringBuilder patchNote = new StringBuilder(START_MESSAGE);
        addAnnouncements(patchNote, announcements);

        List<UserInfo> users = userInfoRepository.findAll();

        makeAnnounce(patchNote.toString(), users);

        announcements.forEach(announcement -> announcement.setAnnounced(true));
        announcementRepository.saveAll(announcements);
    }

    private void addAnnouncements(StringBuilder patchNote, List<Announcement> announcements) {
        for (Announcement announcement : announcements) {
            patchNote.append("◾");
            patchNote.append(announcement.getText());
            patchNote.append(System.lineSeparator());
            patchNote.append(System.lineSeparator());
        }
    }

    private void makeAnnounce(String patchNote, List<UserInfo> users) {
        for (UserInfo user : users) {
            SendMessage message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(patchNote)
                .build();

            try {
                telegramService.sendMessage(message);
            } catch (MessageSenderException e) {
                log.warn("Exception while sending announcement for user with id {}. {}", user.getChatId(),
                    e.getMessage());
            }
        }
    }
}
