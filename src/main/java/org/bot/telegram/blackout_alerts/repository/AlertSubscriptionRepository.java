package org.bot.telegram.blackout_alerts.repository;

import org.bot.telegram.blackout_alerts.model.entity.AlertSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription, Long> {
}
