package org.bot.telegram.blackout_alerts.service;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.AlertSubscription;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.AlertSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AlertSubscriptionService {

    private final AddressService addressService;

    private final ScheduleService scheduleService;

    private final AlertSubscriptionRepository alertSubscriptionRepository;

    public void subscribeToAlert(UserSession session) {
        log.info("Chat id: {}. Subscribing to alert", session.getChatId());
        scheduleService.getShutdownScheduleFromWeb(session);
        AddressEntity address = addressService.getAddressEntity(session);

        AlertSubscription subscription = new AlertSubscription();
        subscription.setChatId(session.getChatId());
        subscription.setAddress(address);
        alertSubscriptionRepository.save(subscription);
        log.info("Chat id: {}. Successfully subscribed to alert", session.getChatId());
    }

    public void unsubscribeFromAlert(long chatId) {
        log.info("Chat id: {}. Unsubscribing from alert", chatId);
        alertSubscriptionRepository.deleteById(chatId);
        log.info("Chat id: {}. Successfully unsubscribed from alert", chatId);
    }

    public Optional<AlertSubscription> getAlertSubscription(long chatId) {
        return alertSubscriptionRepository.findById(chatId);
    }

    public List<AlertSubscription> getAllAlertSubscriptions() {
        return alertSubscriptionRepository.findAll();
    }
}
