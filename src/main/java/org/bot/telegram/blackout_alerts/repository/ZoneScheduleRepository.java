package org.bot.telegram.blackout_alerts.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneScheduleRepository extends JpaRepository<ZoneSchedule, String> {

    Optional<ZoneSchedule> findByZoneAndExpireDateAfter(String zone, LocalDateTime expireDateAfter);
}
