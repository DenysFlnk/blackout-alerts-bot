package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.parseSchedule;
import static org.bot.telegram.blackout_alerts.util.ScheduleUtil.renderTodaySchedule;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.entity.Zone;
import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.model.session.Address;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.ZoneScheduleRepository;
import org.bot.telegram.blackout_alerts.service.browser.BrowserInteractionService;
import org.bot.telegram.blackout_alerts.util.ScheduleUtil;
import org.openqa.selenium.WebDriverException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleService {

    private final BrowserInteractionService browserService;

    private final AddressService addressService;

    private final ZoneScheduleRepository scheduleRepository;

    public ByteArrayInputStream getWeekScheduleScreenshot(UserSession session) {
        ByteArrayInputStream screenshot = browserService.getWeekShutdownScheduleScreenshot(session);
        addressService.updateAddressInDb(session);
        log.info("Chat id: {}. Success getting week shutdown schedule screenshot", session.getChatId());
        return screenshot;
    }

    public String getRenderedTodaySchedule(UserSession session) {
        Schedule schedule = getShutdownScheduleFromDb(session)
            .orElseGet(() -> getShutdownScheduleFromWeb(session));
        return renderTodaySchedule(schedule);
    }

    private Optional<Schedule> getShutdownScheduleFromDb(UserSession session) {
        log.info("Chat id: {}. Trying to get shutdown schedule from DB", session.getChatId());
        Optional<Address> addressOptional = addressService.getAddressFromDb(session);

        if (addressOptional.isPresent()) {
            log.info("Chat id: {}. Address is present in DB", session.getChatId());
            Address address = addressOptional.get();
            session.setAddress(address);

            Optional<ZoneSchedule> zoneSchedule = scheduleRepository.findByZoneAndExpireDateAfter(
                Zone.findZone(address.getCity()), LocalDateTime.now());

            return zoneSchedule.map(schedule -> parseSchedule(schedule.getScheduleJson(), address.getShutdownGroup()));
        }

        log.info("Chat id: {}. Address {}, {}, {} in DB not found", session.getChatId(), session.getUserCity(),
            session.getUserStreet(), session.getUserHouse());
        return Optional.empty();
    }

    private Schedule getShutdownScheduleFromWeb(UserSession session) {
        log.info("Chat id: {}. Getting shutdown schedule from web", session.getChatId());
        try {
            String scheduleJson = browserService.getShutDownSchedule(session);

            addressService.updateAddressInDb(session);
            scheduleRepository.save(ScheduleUtil.getZoneSchedule(session, scheduleJson));

            log.info("Chat id: {}. Success getting shutdown schedule from web", session.getChatId());
            return parseSchedule(scheduleJson, session.getShutdownGroup());
        } catch (WebDriverException | InvalidAddressException e) {
            log.warn("Chat id: {}. Got an '{}' while getting shutdown schedule from web. "
                + "Trying to find address in DB", session.getChatId(), e.getClass().getSimpleName());
            Optional<Address> addressOptional = addressService.getAddressFromDb(session);

            if (addressOptional.isPresent()) {
                log.info("Chat id: {}. Found address in DB {}", session.getChatId(), addressOptional.get());
                Address address = addressOptional.get();
                Zone zone = Zone.findZone(address.getCity());
                ZoneSchedule zoneSchedule = scheduleRepository.findById(zone).orElseThrow(
                    () -> new NoSuchElementException(String.format("Schedule for zone %s not found", zone.name())));
                return parseSchedule(zoneSchedule.getScheduleJson(), address.getShutdownGroup());
            }

            throw e;
        }
    }
}
