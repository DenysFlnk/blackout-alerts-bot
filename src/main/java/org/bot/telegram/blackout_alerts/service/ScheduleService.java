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

    public ByteArrayInputStream getWeekScheduleScreenshot(UserSession userSession) {
        ByteArrayInputStream screenshot = browserService.getWeekShutdownScheduleScreenshot(userSession);
        addressService.updateAddressInDb(userSession);
        log.info("Chat id: {}. Success getting week shutdown schedule screenshot", userSession.getChatId());
        return screenshot;
    }

    public String getRenderedTodaySchedule(UserSession userSession) {
        Schedule schedule = getShutdownScheduleFromDb(userSession)
            .orElseGet(() -> getShutdownScheduleFromWeb(userSession));
        return renderTodaySchedule(schedule);
    }

    private Optional<Schedule> getShutdownScheduleFromDb(UserSession userSession) {
        log.info("Chat id: {}. Trying to get shutdown schedule from DB", userSession.getChatId());
        Optional<Address> addressOptional = addressService.getAddressFromDb(userSession);

        if (addressOptional.isPresent()) {
            log.info("Chat id: {}. Address is present in DB", userSession.getChatId());
            Address address = addressOptional.get();
            userSession.setAddress(address);

            Optional<ZoneSchedule> zoneSchedule = scheduleRepository.findByZoneAndExpireDateAfter(
                Zone.findZone(address.getCity()), LocalDateTime.now());

            return zoneSchedule.map(schedule -> parseSchedule(schedule.getScheduleJson(), address.getShutdownGroup()));
        }

        log.info("Chat id: {}. Address {}, {}, {} in DB not found", userSession.getChatId(), userSession.getUserCity(),
            userSession.getUserStreet(), userSession.getUserHouse());
        return Optional.empty();
    }

    private Schedule getShutdownScheduleFromWeb(UserSession userSession) {
        log.info("Chat id: {}. Getting shutdown schedule from web", userSession.getChatId());
        try {
            String scheduleJson = browserService.getShutDownSchedule(userSession);

            addressService.updateAddressInDb(userSession);
            scheduleRepository.save(ScheduleUtil.getZoneSchedule(userSession, scheduleJson));

            log.info("Chat id: {}. Success getting shutdown schedule from web", userSession.getChatId());
            return parseSchedule(scheduleJson, userSession.getShutdownGroup());
        } catch (WebDriverException | InvalidAddressException e) {
            log.warn("Chat id: {}. Got an '{}' while getting shutdown schedule from web. "
                + "Trying to find address in DB", userSession.getChatId(), e.getClass().getSimpleName());
            Optional<Address> addressOptional = addressService.getAddressFromDb(userSession);

            if (addressOptional.isPresent()) {
                log.info("Chat id: {}. Found address in DB {}", userSession.getChatId(), addressOptional.get());
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
