package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ZoneSchedule {

    @Id
    private Zone zone;

    private String scheduleJson;

    private LocalDate expireDate;
}
