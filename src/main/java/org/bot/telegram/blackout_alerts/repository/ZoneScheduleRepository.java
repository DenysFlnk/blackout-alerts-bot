package org.bot.telegram.blackout_alerts.repository;

import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneScheduleRepository extends JpaRepository<ZoneSchedule, String> {
}
