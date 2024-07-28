package org.bot.telegram.blackout_alerts.service.alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.MessageSenderException;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.AlertSubscription;
import org.bot.telegram.blackout_alerts.model.entity.Zone;
import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.schedule.Possibility;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.repository.ZoneScheduleRepository;
import org.bot.telegram.blackout_alerts.service.AlertSubscriptionService;
import org.bot.telegram.blackout_alerts.service.TelegramService;
import org.bot.telegram.blackout_alerts.util.ScheduleUtil;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlertScheduler {

    private static final String ALERT_MESSAGE_FORMAT = """
        За адресою:
        %s, %s, %s
        
        🚨 Можливе відключення через 15 хв 🚨
        
        Згідно графіку погодинних відключень 🗓️
        """;

    private final TelegramService telegramService;

    private final AlertSubscriptionService alertSubscriptionService;

    private final ZoneScheduleRepository scheduleRepository;

    private final Map<Byte, Schedule> kyivZoneScheduleMap = new HashMap<>();
    private final Map<Byte, Schedule> regionZoneScheduleMap = new HashMap<>();

    private final LocalDate scheduleExpire = LocalDate.now().plusDays(3);
    private static final ZoneId UTC_PLUS_3 = ZoneId.of("UTC+3");

    @Scheduled(cron = "0 45 * * * *")
    private void sendAlert() {
        log.info("AlertScheduler is up");
        LocalDateTime now = LocalDateTime.now(UTC_PLUS_3);

        if (isRestrictedHours(now.toLocalTime())) {
            log.info("Skipping alerts due to restricted hours");
            return;
        }

        updateZoneScheduleMaps();

        List<AlertSubscription> subscriptions = alertSubscriptionService.getAllAlertSubscriptions();
        for (AlertSubscription subscription : subscriptions) {
            Zone zone = Zone.findZone(subscription.getAddress().getCity());
            byte group = subscription.getAddress().getShutdownGroup();

            if (isShutdownIncoming(zone, group, now)) {
                sendNotification(subscription);
            }
        }
    }

    public static boolean isRestrictedHours(LocalTime time) {
        return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(8, 0));
    }

    private void updateZoneScheduleMaps() {
        if (LocalDate.now().isAfter(scheduleExpire) || kyivZoneScheduleMap.isEmpty() || regionZoneScheduleMap.isEmpty()) {
            int zoneCount = ShutDownSchedule.class.getDeclaredFields().length;
            updateZoneScheduleMap(kyivZoneScheduleMap, Zone.KYIV, zoneCount);
            updateZoneScheduleMap(regionZoneScheduleMap, Zone.REGIONS, zoneCount);
        }
    }

    private void updateZoneScheduleMap(Map<Byte, Schedule> map, Zone zone, int zoneCount) {
        Optional<ZoneSchedule> zoneScheduleOptional = scheduleRepository.findById(zone);

        if (zoneScheduleOptional.isPresent()) {
            map.clear();
            ZoneSchedule zoneSchedule = zoneScheduleOptional.get();

            for (byte i = 1; i <= zoneCount; i++) {
                Schedule schedule = ScheduleUtil.parseSchedule(zoneSchedule.getScheduleJson(), i);
                map.put(i, schedule);
            }
        }
    }

    private boolean isShutdownIncoming(Zone zone, byte group, LocalDateTime currentTime) {
        Map<Byte, Schedule> scheduleMap = zone == Zone.KYIV ? kyivZoneScheduleMap : regionZoneScheduleMap;
        Map<Integer, Possibility> hourPossibilityMap = scheduleMap.get(group).getWeekListMap()
            .get(currentTime.getDayOfWeek()).stream()
            .filter(pair -> pair.getFirst().getHour() == currentTime.getHour() ||
                            pair.getFirst().getHour() == currentTime.getHour() + 1)
            .collect(Collectors.toMap(pair -> pair.getFirst().getHour(), Pair::getSecond));

        return isShutdownIncoming(currentTime, hourPossibilityMap);
    }

    private static boolean isShutdownIncoming(LocalDateTime currentTime, Map<Integer, Possibility> hourPossibilityMap) {
        return hourPossibilityMap.get(currentTime.getHour()) == Possibility.YES
               && hourPossibilityMap.get(currentTime.getHour() + 1) == Possibility.NO;
    }

    private void sendNotification(AlertSubscription subscription) {
        log.info("Chat id: {}. Sending alert notification, shutdown group {}", subscription.getChatId(),
            subscription.getAddress().getShutdownGroup());
        AddressEntity address = subscription.getAddress();
        SendMessage message = SendMessage.builder()
            .chatId(subscription.getChatId())
            .text(String.format(ALERT_MESSAGE_FORMAT, address.getCity(),address.getStreet(), address.getHouse()))
            .build();

        try {
            telegramService.sendMessage(message);
        } catch (MessageSenderException e) {
            log.warn("Chat id: {}. Failed to send alert notification", e.getMessage());

            if (e.getCause() instanceof TelegramApiRequestException cause && cause.getErrorCode() == 403) {
                alertSubscriptionService.unsubscribeFromAlert(subscription.getChatId());
            }
        }
    }
}
