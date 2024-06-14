package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.model.schedule.Possibility.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bot.telegram.blackout_alerts.model.schedule.Possibility;
import org.bot.telegram.blackout_alerts.model.schedule.Schedule;
import org.bot.telegram.blackout_alerts.model.json.Group;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.json.TimeZone;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import wagu.Board;
import wagu.Table;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final BrowserInteractionService browserService;

    public String getRenderedTodaySchedule(UserSession userSession) {
        Schedule schedule = userSession.getSchedule();

        if (schedule != null && !isScheduleExpired(schedule)) {
            return renderTodaySchedule(schedule);
        }

        ShutDownSchedule shutDownSchedule = browserService.getShutDownSchedule(userSession);
        schedule = parseSchedule(userSession, shutDownSchedule);
        userSession.setSchedule(schedule);

        return renderTodaySchedule(schedule);
    }

    public String renderTodaySchedule(Schedule schedule) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Pair<String, Possibility>> currentDayPossibilities = schedule.getWeekListMap().get(today);
        List<String> headerList = Arrays.asList("Часовий інтервал (год)", "Можливість відкючення");
        List<List<String>> rowList = currentDayPossibilities.stream()
            .map(pair -> Arrays.asList(pair.getFirst(), pair.getSecond().toString()))
            .toList();

        Board board = new Board(75);
        Table table = new Table(board, 75, headerList, rowList);
        board.setInitialBlock(table.tableToBlocks());
        board.build();

        return board.getPreview();
    }

    private static boolean isScheduleExpired(Schedule schedule) {
        return !LocalDate.now().isBefore(schedule.getExpireDate());
    }

    private static Schedule parseSchedule(UserSession userSession, ShutDownSchedule shutDownSchedule) {
        Schedule schedule = new Schedule(userSession.getChatId());
        schedule.setExpireDate(getScheduleExpireDate());

        Group shutdownGroup = shutDownSchedule.getGroup(userSession.getShutdownGroup());
        Map<DayOfWeek, List<Pair<String, Possibility>>> map = new HashMap<>();

        TimeZone monday = shutdownGroup.getMonday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(monday));

        TimeZone tuesday = shutdownGroup.getTuesday();
        map.put(DayOfWeek.TUESDAY, getListOfPossibilitiesForADay(tuesday));

        TimeZone wednesday = shutdownGroup.getWednesday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(wednesday));

        TimeZone thursday = shutdownGroup.getThursday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(thursday));

        TimeZone friday = shutdownGroup.getFriday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(friday));

        TimeZone saturday = shutdownGroup.getSaturday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(saturday));

        TimeZone sunday = shutdownGroup.getSunday();
        map.put(DayOfWeek.MONDAY, getListOfPossibilitiesForADay(sunday));

        schedule.setWeekListMap(map);

        return schedule;
    }

    private static LocalDate getScheduleExpireDate() {
        return LocalDate.now().plusDays(7);
    }

    private static List<Pair<String, Possibility>> getListOfPossibilitiesForADay(TimeZone timeZone) {
        List<Pair<String, Possibility>> possibilities = new ArrayList<>();

        possibilities.add(Pair.of("00-01", valueOf(timeZone.getT00_01().toUpperCase())));
        possibilities.add(Pair.of("01-02", valueOf(timeZone.getT01_02().toUpperCase())));
        possibilities.add(Pair.of("02-03", valueOf(timeZone.getT02_03().toUpperCase())));
        possibilities.add(Pair.of("03-04", valueOf(timeZone.getT03_04().toUpperCase())));
        possibilities.add(Pair.of("04-05", valueOf(timeZone.getT04_05().toUpperCase())));
        possibilities.add(Pair.of("05-06", valueOf(timeZone.getT05_06().toUpperCase())));
        possibilities.add(Pair.of("06-07", valueOf(timeZone.getT06_07().toUpperCase())));
        possibilities.add(Pair.of("07-08", valueOf(timeZone.getT07_08().toUpperCase())));
        possibilities.add(Pair.of("08-09", valueOf(timeZone.getT08_09().toUpperCase())));
        possibilities.add(Pair.of("09-10", valueOf(timeZone.getT09_10().toUpperCase())));
        possibilities.add(Pair.of("10-11", valueOf(timeZone.getT10_11().toUpperCase())));
        possibilities.add(Pair.of("11-12", valueOf(timeZone.getT11_12().toUpperCase())));
        possibilities.add(Pair.of("12-13", valueOf(timeZone.getT12_13().toUpperCase())));
        possibilities.add(Pair.of("13-14", valueOf(timeZone.getT13_14().toUpperCase())));
        possibilities.add(Pair.of("14-15", valueOf(timeZone.getT14_15().toUpperCase())));
        possibilities.add(Pair.of("15-16", valueOf(timeZone.getT15_16().toUpperCase())));
        possibilities.add(Pair.of("16-17", valueOf(timeZone.getT16_17().toUpperCase())));
        possibilities.add(Pair.of("17-18", valueOf(timeZone.getT17_18().toUpperCase())));
        possibilities.add(Pair.of("18-19", valueOf(timeZone.getT18_19().toUpperCase())));
        possibilities.add(Pair.of("19-20", valueOf(timeZone.getT19_20().toUpperCase())));
        possibilities.add(Pair.of("20-21", valueOf(timeZone.getT20_21().toUpperCase())));
        possibilities.add(Pair.of("21-22", valueOf(timeZone.getT21_22().toUpperCase())));
        possibilities.add(Pair.of("22-23", valueOf(timeZone.getT22_23().toUpperCase())));
        possibilities.add(Pair.of("23-24", valueOf(timeZone.getT23_24().toUpperCase())));

        return possibilities;
    }
}
