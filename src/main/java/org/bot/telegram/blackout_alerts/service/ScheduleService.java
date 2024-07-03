package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.parseSchedule;
import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.renderTodaySchedule;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.Zone;
import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.AddressEntityRepository;
import org.bot.telegram.blackout_alerts.repository.ZoneScheduleRepository;
import org.bot.telegram.blackout_alerts.service.browser.BrowserInteractionService;
import org.bot.telegram.blackout_alerts.util.ScheduleUtil;
import org.bot.telegram.blackout_alerts.util.UserSessionUtil;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleService {

    private final BrowserInteractionService browserService;

    private final AddressEntityRepository addressRepository;

    private final ZoneScheduleRepository scheduleRepository;

    public String getRenderedTodaySchedule(UserSession userSession) {
        Schedule schedule = getShutdownScheduleFromDb(userSession)
            .orElseGet(() -> getShutdownScheduleFromWeb(userSession));
        return renderTodaySchedule(schedule);
    }

    private Optional<Schedule> getShutdownScheduleFromDb(UserSession userSession) {
        log.info("Chat id: {}. Trying to get shutdown schedule from DB", userSession.getChatId());
        Optional<AddressEntity> addressEntity = addressRepository.findByCityContainsAndStreetContainsAndHouse(
            userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse());

        if (addressEntity.isPresent()) {
            log.info("Chat id: {}. Address is present in DB", userSession.getChatId());
            AddressEntity address = addressEntity.get();
            userSession.setAddress(UserSessionUtil.getAddress(address));

            Optional<ZoneSchedule> zoneSchedule = scheduleRepository.findByZoneAndExpireDateAfter(
                Zone.findZone(address.getCity()), LocalDateTime.now());

            return zoneSchedule.map(schedule -> parseSchedule(schedule.getScheduleJson(), address.getShutdownGroup()));
        }

        log.info("Address {}, {}, {} in DB not found", userSession.getUserCity(), userSession.getUserStreet(),
            userSession.getUserHouse());
        return Optional.empty();
    }

    private Schedule getShutdownScheduleFromWeb(UserSession userSession) {
        log.info("Chat id: {}. Getting shutdown schedule from web", userSession.getChatId());
        try {
            String scheduleJson = browserService.getShutDownSchedule(userSession);

            addressRepository.save(UserSessionUtil.getAddressEntity(userSession));
            scheduleRepository.save(ScheduleUtil.getZoneSchedule(userSession, scheduleJson));

            log.info("Chat id: {}. Success getting shutdown schedule from web", userSession.getChatId());
            return parseSchedule(scheduleJson, userSession.getShutdownGroup());
        } catch (InvalidAddressException e) {
            log.warn("Got an exception while getting shutdown schedule from web. Trying to find address in DB");
            Optional<AddressEntity> addressEntity = addressRepository.findByCityContainsAndStreetContainsAndHouse(
                userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse());

            if (addressEntity.isPresent()) {
                log.info("Found address in DB {}", addressEntity.get());
                AddressEntity address = addressEntity.get();
                Zone zone = Zone.findZone(address.getCity());
                ZoneSchedule zoneSchedule = scheduleRepository.findById(zone).orElseThrow(
                    () -> new NoSuchElementException(String.format("Schedule for zone %s not found", zone.name())));
                return parseSchedule(zoneSchedule.getScheduleJson(), address.getShutdownGroup());
            }

            throw e;
        }
    }
}
