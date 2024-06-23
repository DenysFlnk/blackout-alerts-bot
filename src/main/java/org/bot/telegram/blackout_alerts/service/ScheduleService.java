package org.bot.telegram.blackout_alerts.service;

import static com.vdurmont.emoji.EmojiParser.parseToUnicode;
import static org.bot.telegram.blackout_alerts.model.schedule.Possibility.valueOf;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.model.json.Group;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.json.TimeZone;
import org.bot.telegram.blackout_alerts.model.schedule.Possibility;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.service.browser.BrowserInteractionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import wagu.Block;
import wagu.Board;
import wagu.Table;

@Service
@AllArgsConstructor
public class ScheduleService {

    private static final Map<Possibility, String> possibilityToEmojiMap = new EnumMap<>(Possibility.class);

    static {
        possibilityToEmojiMap.put(Possibility.YES, "\uD83D\uDFE2");
        possibilityToEmojiMap.put(Possibility.NO, "\uD83D\uDD34");
        possibilityToEmojiMap.put(Possibility.MAYBE, "\uD83D\uDFE1");
    }

    private final BrowserInteractionService browserService;

    public String getRenderedTodaySchedule(UserSession userSession) {
        if (isUserScheduleValid(userSession)) {
            return renderTodaySchedule(userSession.getSchedule());
        }

        if (!tryToSetShutdownScheduleFromDb(userSession)) {
            setShutdownScheduleFromBrowser(userSession);
        }

        return renderTodaySchedule(userSession.getSchedule());
    }

    private void setShutdownScheduleFromBrowser(UserSession userSession) {
        ShutDownSchedule shutDownSchedule = browserService.getShutDownSchedule(userSession);
        userSession.setSchedule(parseSchedule(userSession, shutDownSchedule));
    }

    private boolean tryToSetShutdownScheduleFromDb(UserSession userSession) {
        //get shutdown group from address table
        //set group
        //get schedule by group from schedule table
        //set schedule
        return false;
    }

    private static boolean isUserScheduleValid(UserSession userSession) {
        Schedule schedule = userSession.getSchedule();
        return schedule != null && !isScheduleExpired(schedule) && schedule.getAddress().equals(userSession.getAddress());
    }

    private String renderTodaySchedule(Schedule schedule) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Pair<String, String>> currentDayPossibilities = schedule.getWeekListMap().get(today);
        List<String> headerList = Arrays.asList(parseToUnicode(":clock5:"), parseToUnicode(":bulb:"));
        List<List<String>> rowList = currentDayPossibilities.stream()
            .map(pair -> Arrays.asList(pair.getFirst(), pair.getSecond()))
            .toList();

        Board board = new Board(30);
        Table table = new Table(board, 30, headerList, rowList);
        table.setColAlignsList(Arrays.asList(Block.DATA_CENTER, Block.DATA_CENTER));
        table.setRowHeight(120);
        board.setInitialBlock(table.tableToBlocks());
        board.build();

        return String.format("""
            <pre>          %s
            %s
            \uD83D\uDFE2 - світло є
            \uD83D\uDD34 - світло відсутнє
            \uD83D\uDFE1 - можливе відключення
            </pre>""", LocalDate.now(), board.getPreview());
    }

    private static boolean isScheduleExpired(Schedule schedule) {
        return !LocalDate.now().isBefore(schedule.getExpireDate());
    }

    private static Schedule parseSchedule(UserSession userSession, ShutDownSchedule shutDownSchedule) {
        Schedule schedule = new Schedule(userSession.getChatId(), userSession.getAddress());
        schedule.setExpireDate(getScheduleExpireDate());

        Group shutdownGroup = shutDownSchedule.getGroup(userSession.getShutdownGroup());
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

        schedule.setWeekListMap(map);

        return schedule;
    }

    private static LocalDate getScheduleExpireDate() {
        return LocalDate.now().plusDays(7);
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
