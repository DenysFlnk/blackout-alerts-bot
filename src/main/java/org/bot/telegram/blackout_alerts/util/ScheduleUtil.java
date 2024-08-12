package org.bot.telegram.blackout_alerts.util;

import static org.bot.telegram.blackout_alerts.model.schedule.Possibility.valueOf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        </pre>""";

    static {
        possibilityToEmojiMap.put(Possibility.YES, "\uD83D\uDFE2");
        possibilityToEmojiMap.put(Possibility.NO, "\uD83D\uDD34");
        possibilityToEmojiMap.put(Possibility.MAYBE, "\uD83D\uDFE1");
    }

    private ScheduleUtil() {
    }

    public static LocalDateTime getScheduleExpireDate() {
        return LocalDateTime.now().plusDays(7);
    }

    public static String renderTodaySchedule(Schedule schedule) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Pair<String, String>> currentDayPossibilities = schedule.getWeekListMap().get(today);
        List<String> headerList = Arrays.asList("\uD83D\uDD54", "\uD83D\uDCA1");
        List<List<String>> rowList = currentDayPossibilities.stream()
            .map(pair -> Arrays.asList(pair.getFirst(), pair.getSecond()))
            .toList();

        Board board = new Board(30);
        Table table = new Table(board, 30, headerList, rowList);
        table.setColAlignsList(Arrays.asList(Block.DATA_CENTER, Block.DATA_CENTER));
        table.setRowHeight(120);
        board.setInitialBlock(table.tableToBlocks());
        board.build();

        return String.format(TABLE_LEGEND_FORMAT, LocalDate.now(), board.getPreview());
    }

    public static ZoneSchedule getZoneSchedule(UserSession session, String scheduleJson) {
        ZoneSchedule zoneSchedule = new ZoneSchedule();
        zoneSchedule.setZone(Zone.findZone(session.getUserCity()));
        zoneSchedule.setScheduleJson(scheduleJson);
        zoneSchedule.setExpireDate(getScheduleExpireDate());
        return zoneSchedule;
    }

    public static Schedule parseSchedule(String scheduleJson, byte group) {
        TypeToken<ShutDownSchedule> token = new TypeToken<>() {};
        ShutDownSchedule shutDownSchedule =  new Gson().fromJson(scheduleJson, token.getType());

        Group shutdownGroup = shutDownSchedule.getGroup(group);
        Map<DayOfWeek, List<Pair<String, String>>> map = new LinkedHashMap<>();

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

    private static List<Pair<String, String>> getListOfPossibilitiesForADay(TimeZone timeZone) {
        List<Pair<String, String>> possibilities = new ArrayList<>();

        possibilities.add(Pair.of("00-01", getPossibilityEmoji(timeZone.getT00_01())));
        possibilities.add(Pair.of("01-02", getPossibilityEmoji(timeZone.getT01_02())));
        possibilities.add(Pair.of("02-03", getPossibilityEmoji(timeZone.getT02_03())));
        possibilities.add(Pair.of("03-04", getPossibilityEmoji(timeZone.getT03_04())));
        possibilities.add(Pair.of("04-05", getPossibilityEmoji(timeZone.getT04_05())));
        possibilities.add(Pair.of("05-06", getPossibilityEmoji(timeZone.getT05_06())));
        possibilities.add(Pair.of("06-07", getPossibilityEmoji(timeZone.getT06_07())));
        possibilities.add(Pair.of("07-08", getPossibilityEmoji(timeZone.getT07_08())));
        possibilities.add(Pair.of("08-09", getPossibilityEmoji(timeZone.getT08_09())));
        possibilities.add(Pair.of("09-10", getPossibilityEmoji(timeZone.getT09_10())));
        possibilities.add(Pair.of("10-11", getPossibilityEmoji(timeZone.getT10_11())));
        possibilities.add(Pair.of("11-12", getPossibilityEmoji(timeZone.getT11_12())));
        possibilities.add(Pair.of("12-13", getPossibilityEmoji(timeZone.getT12_13())));
        possibilities.add(Pair.of("13-14", getPossibilityEmoji(timeZone.getT13_14())));
        possibilities.add(Pair.of("14-15", getPossibilityEmoji(timeZone.getT14_15())));
        possibilities.add(Pair.of("15-16", getPossibilityEmoji(timeZone.getT15_16())));
        possibilities.add(Pair.of("16-17", getPossibilityEmoji(timeZone.getT16_17())));
        possibilities.add(Pair.of("17-18", getPossibilityEmoji(timeZone.getT17_18())));
        possibilities.add(Pair.of("18-19", getPossibilityEmoji(timeZone.getT18_19())));
        possibilities.add(Pair.of("19-20", getPossibilityEmoji(timeZone.getT19_20())));
        possibilities.add(Pair.of("20-21", getPossibilityEmoji(timeZone.getT20_21())));
        possibilities.add(Pair.of("21-22", getPossibilityEmoji(timeZone.getT21_22())));
        possibilities.add(Pair.of("22-23", getPossibilityEmoji(timeZone.getT22_23())));
        possibilities.add(Pair.of("23-24", getPossibilityEmoji(timeZone.getT23_24())));

        return possibilities;
    }

    private static String getPossibilityEmoji(String timeZoneValue) {
        Possibility possibility = valueOf(timeZoneValue.toUpperCase());
        return possibilityToEmojiMap.get(possibility);
    }
}
