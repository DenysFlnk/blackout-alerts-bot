package org.bot.telegram.blackout_alerts.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bot.telegram.blackout_alerts.model.entity.Zone;
import org.bot.telegram.blackout_alerts.model.entity.ZoneSchedule;
import org.bot.telegram.blackout_alerts.model.json.Group;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.json.TimeZone;
import org.bot.telegram.blackout_alerts.model.schedule.Possibility;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.springframework.data.util.Pair;
import wagu.Block;
import wagu.Board;
import wagu.Table;

public class ScheduleUtil {

    private static final Map<Possibility, String> possibilityToEmojiMap = new EnumMap<>(Possibility.class);

    private static final String TABLE_LEGEND_FORMAT = """
        <pre>          %s
        %s
        \uD83D\uDFE2 - світло є
        \uD83D\uDD34 - світло відсутнє
        \uD83D\uDFE1 - можливе відключення
        \uD83D\uDD35 - світла не буде перші 30хв
        \uD83D\uDFE0 - світла не буде другі 30хв
        </pre>""";

    static {
        possibilityToEmojiMap.put(Possibility.YES, "\uD83D\uDFE2");
        possibilityToEmojiMap.put(Possibility.NO, "\uD83D\uDD34");
        possibilityToEmojiMap.put(Possibility.MAYBE, "\uD83D\uDFE1");
        possibilityToEmojiMap.put(Possibility.FIRST, "\uD83D\uDD35");
        possibilityToEmojiMap.put(Possibility.SECOND, "\uD83D\uDFE0");
    }

    public static final ZoneId UTC_PLUS_3 = ZoneId.of("UTC+3");

    private ScheduleUtil() {
    }

    public static LocalDateTime getScheduleExpireDate() {
        return LocalDateTime.now(UTC_PLUS_3).plusDays(7);
    }

    public static String renderTodaySchedule(Schedule schedule) {
        LocalDate now = LocalDate.now(UTC_PLUS_3);
        DayOfWeek today = now.getDayOfWeek();
        List<Pair<LocalTime, Possibility>> currentDayPossibilities = schedule.getWeekListMap().get(today);
        List<String> headerList = Arrays.asList("\uD83D\uDD54", "\uD83D\uDCA1");
        List<List<String>> rowList = currentDayPossibilities.stream()
            .map(pair -> Arrays.asList(parseTimeRange(pair.getFirst()), possibilityToEmojiMap.get(pair.getSecond())))
            .toList();

        Board board = new Board(30);
        Table table = new Table(board, 30, headerList, rowList);
        table.setColAlignsList(Arrays.asList(Block.DATA_CENTER, Block.DATA_CENTER));
        table.setRowHeight(120);
        board.setInitialBlock(table.tableToBlocks());
        board.build();

        return String.format(TABLE_LEGEND_FORMAT, now, board.getPreview());
    }

    public static ZoneSchedule getZoneSchedule(UserSession session, String scheduleJson) {
        ZoneSchedule zoneSchedule = new ZoneSchedule();
        zoneSchedule.setZone(Zone.findZone(session.getUserCity()));
        zoneSchedule.setScheduleJson(scheduleJson);
        zoneSchedule.setExpireDate(getScheduleExpireDate());
        return zoneSchedule;
    }

    public static Schedule parseSchedule(String scheduleJson, String group) {
        TypeToken<ShutDownSchedule> token = new TypeToken<>() {};
        ShutDownSchedule shutDownSchedule =  new Gson().fromJson(scheduleJson, token.getType());

        Group shutdownGroup = shutDownSchedule.getGroup(group);
        Map<DayOfWeek, List<Pair<LocalTime, Possibility>>> map = new LinkedHashMap<>();

        TimeZone monday = shutdownGroup.getMonday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(monday));

        TimeZone tuesday = shutdownGroup.getTuesday();
        map.put(DayOfWeek.TUESDAY, getListOfPossibilitiesForADay(tuesday));

        TimeZone wednesday = shutdownGroup.getWednesday();
        map.put(DayOfWeek.WEDNESDAY, getListOfPossibilitiesForADay(wednesday));

        TimeZone thursday = shutdownGroup.getThursday();
        map.put(DayOfWeek.THURSDAY, getListOfPossibilitiesForADay(thursday));

        TimeZone friday = shutdownGroup.getFriday();
        map.put(DayOfWeek.FRIDAY, getListOfPossibilitiesForADay(friday));

        TimeZone saturday = shutdownGroup.getSaturday();
        map.put(DayOfWeek.SATURDAY, getListOfPossibilitiesForADay(saturday));

        TimeZone sunday = shutdownGroup.getSunday();
        map.put(DayOfWeek.SUNDAY, getListOfPossibilitiesForADay(sunday));

        Schedule schedule = new Schedule();
        schedule.setWeekListMap(map);

        return schedule;
    }

    private static List<Pair<LocalTime, Possibility>> getListOfPossibilitiesForADay(TimeZone timeZone) {
        List<Pair<LocalTime, Possibility>> possibilities = new ArrayList<>();

        possibilities.add(Pair.of(LocalTime.of(0, 0), getPossibility(timeZone.getT00_01())));
        possibilities.add(Pair.of(LocalTime.of(1, 0), getPossibility(timeZone.getT01_02())));
        possibilities.add(Pair.of(LocalTime.of(2, 0), getPossibility(timeZone.getT02_03())));
        possibilities.add(Pair.of(LocalTime.of(3, 0), getPossibility(timeZone.getT03_04())));
        possibilities.add(Pair.of(LocalTime.of(4, 0), getPossibility(timeZone.getT04_05())));
        possibilities.add(Pair.of(LocalTime.of(5, 0), getPossibility(timeZone.getT05_06())));
        possibilities.add(Pair.of(LocalTime.of(6, 0), getPossibility(timeZone.getT06_07())));
        possibilities.add(Pair.of(LocalTime.of(7, 0), getPossibility(timeZone.getT07_08())));
        possibilities.add(Pair.of(LocalTime.of(8, 0), getPossibility(timeZone.getT08_09())));
        possibilities.add(Pair.of(LocalTime.of(9, 0), getPossibility(timeZone.getT09_10())));
        possibilities.add(Pair.of(LocalTime.of(10, 0), getPossibility(timeZone.getT10_11())));
        possibilities.add(Pair.of(LocalTime.of(11, 0), getPossibility(timeZone.getT11_12())));
        possibilities.add(Pair.of(LocalTime.of(12, 0), getPossibility(timeZone.getT12_13())));
        possibilities.add(Pair.of(LocalTime.of(13, 0), getPossibility(timeZone.getT13_14())));
        possibilities.add(Pair.of(LocalTime.of(14, 0), getPossibility(timeZone.getT14_15())));
        possibilities.add(Pair.of(LocalTime.of(15, 0), getPossibility(timeZone.getT15_16())));
        possibilities.add(Pair.of(LocalTime.of(16, 0), getPossibility(timeZone.getT16_17())));
        possibilities.add(Pair.of(LocalTime.of(17, 0), getPossibility(timeZone.getT17_18())));
        possibilities.add(Pair.of(LocalTime.of(18, 0), getPossibility(timeZone.getT18_19())));
        possibilities.add(Pair.of(LocalTime.of(19, 0), getPossibility(timeZone.getT19_20())));
        possibilities.add(Pair.of(LocalTime.of(20, 0), getPossibility(timeZone.getT20_21())));
        possibilities.add(Pair.of(LocalTime.of(21, 0), getPossibility(timeZone.getT21_22())));
        possibilities.add(Pair.of(LocalTime.of(22, 0), getPossibility(timeZone.getT22_23())));
        possibilities.add(Pair.of(LocalTime.of(23, 0), getPossibility(timeZone.getT23_24())));

        return possibilities;
    }

    private static Possibility getPossibility(String value) {
        return Possibility.valueOf(value.toUpperCase());
    }

    private static String parseTimeRange(LocalTime time) {
        int hour = time.getHour();
        return String.format("%02d-%02d", hour, hour + 1);
    }
}
