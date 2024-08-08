package org.bot.telegram.blackout_alerts.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.browser.BrowserInteractionService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ShutdownStatusService {

    private final BrowserInteractionService browserService;

    private final AddressService addressService;

    public String getShutdownStatus(UserSession session) {
        String status = browserService.getShutdownStatus(session);
        addressService.updateAddressInDb(session);
        log.info("Chat id: {}. Success getting shutdown status", session.getChatId());
        return status;
    }
}
