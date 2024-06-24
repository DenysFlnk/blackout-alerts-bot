package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zone_schedule")
@Data
@NoArgsConstructor
public class ZoneSchedule {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "zone")
    private Zone zone;

    @Column(name = "schedule_json")
    private String scheduleJson;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;
}
