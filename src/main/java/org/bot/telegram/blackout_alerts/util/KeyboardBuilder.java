package org.bot.telegram.blackout_alerts.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class KeyboardBuilder {

    private static final InlineKeyboardButton enterAddressButton = enterAddressButton();
    private static final InlineKeyboardButton changeAddressButton = changeAddressButton();
    private static final InlineKeyboardButton showAddressButton = showAddressButton();
    private static final InlineKeyboardButton showScheduleButton = showScheduleButton();
    private static final InlineKeyboardButton showWeekScheduleButton = showWeekScheduleButton();
    private static final InlineKeyboardButton checkShutdownStatusButton = checkShutdownStatusButton();

    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    public static KeyboardBuilder builder() {
        return new KeyboardBuilder();
    }

    public KeyboardBuilder addEnterAddressButton() {
        keyboard.add(Collections.singletonList(enterAddressButton));
        return this;
    }

    public KeyboardBuilder addChangeAddressButton() {
        keyboard.add(Collections.singletonList(changeAddressButton));
        return this;
    }

    public KeyboardBuilder addShowAddressButton() {
        keyboard.add(Collections.singletonList(showAddressButton));
        return this;
    }

    public KeyboardBuilder addShowScheduleButton() {
        keyboard.add(Collections.singletonList(showScheduleButton));
        return this;
    }

    public KeyboardBuilder addShowWeekScheduleButton() {
        keyboard.add(Collections.singletonList(showWeekScheduleButton));
        return this;
    }

    public KeyboardBuilder addCheckShutdownStatusButton() {
        keyboard.add(Collections.singletonList(checkShutdownStatusButton));
        return this;
    }

    public InlineKeyboardMarkup build() {
        return InlineKeyboardMarkup.builder()
            .keyboard(keyboard)
            .build();
    }

    private static InlineKeyboardButton enterAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Ввести адресу \uD83C\uDFD8");
        button.setCallbackData("/enter_address");
        return button;
    }

    private static InlineKeyboardButton changeAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Змінити адресу \uD83D\uDD04");
        button.setCallbackData("/change_address");
        return button;
    }

    private static InlineKeyboardButton showAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Показати збережену адресу \uD83D\uDC40");
        button.setCallbackData("/show_address");
        return button;
    }

    private static InlineKeyboardButton showScheduleButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Отримати графік на сьогодні \uD83D\uDCA1");
        button.setCallbackData("/today_schedule");
        return button;
    }

    private static InlineKeyboardButton showWeekScheduleButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Отримати графік на тиждень \uD83D\uDCC5");
        button.setCallbackData("/week_schedule");
        return button;
    }

    private static InlineKeyboardButton checkShutdownStatusButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Перевірити поточний статус відключення \uD83D\uDD0E");
        button.setCallbackData("/check_shutdown_status");
        return button;
    }
}
