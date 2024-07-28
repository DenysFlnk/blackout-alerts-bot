package org.bot.telegram.blackout_alerts.model.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.data.util.Pair;

@Data
public class Schedule {

    private Map<DayOfWeek, List<Pair<LocalTime, Possibility>>> weekListMap;
}
