package org.bot.telegram.blackout_alerts.repository;

import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressEntityRepository extends JpaRepository<AddressEntity, Integer> {
}
