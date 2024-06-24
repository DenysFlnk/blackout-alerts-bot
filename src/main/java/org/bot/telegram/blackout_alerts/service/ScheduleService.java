package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.getScheduleExpireDate;
import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.parseSchedule;
import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.renderTodaySchedule;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Schedule schedule = getShutdownScheduleFromDb(userSession).orElse(getShutdownScheduleFromWeb(userSession));
        return renderTodaySchedule(schedule);
    }

    private Optional<Schedule> getShutdownScheduleFromDb(UserSession userSession) {
        log.info("Trying to get shutdown schedule from DB.");
        Optional<AddressEntity> addressEntity = addressRepository.findByCityAndStreetAndHouse(userSession.getUserCity(),
            userSession.getUserStreet(), userSession.getUserHouse());

        if (addressEntity.isPresent()) {
            AddressEntity address = addressEntity.get();
            Optional<ZoneSchedule> zoneSchedule = scheduleRepository.findByZoneAndExpireDateAfter(
                Zone.findZone(address.getCity()).name(), getScheduleExpireDate());

            return zoneSchedule.map(schedule -> parseSchedule(schedule.getScheduleJson(), address.getShutdownGroup()));
        }

        log.info("Address {}, {}, {} in DB not found.", userSession.getUserCity(), userSession.getUserStreet(),
            userSession.getUserHouse());
        return Optional.empty();
    }

    private Schedule getShutdownScheduleFromWeb(UserSession userSession) {
        log.info("Getting shutdown schedule from web.");
        String scheduleJson = browserService.getShutDownSchedule(userSession);

        addressRepository.save(UserSessionUtil.getAddressEntity(userSession));
        scheduleRepository.save(ScheduleUtil.getZoneSchedule(userSession, scheduleJson));

        return parseSchedule(scheduleJson, userSession.getShutdownGroup());
    }
}
