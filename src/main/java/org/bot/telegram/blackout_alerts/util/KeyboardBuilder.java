package org.bot.telegram.blackout_alerts.util;

import com.vdurmont.emoji.EmojiParser;
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

    public InlineKeyboardMarkup build() {
        return InlineKeyboardMarkup.builder()
            .keyboard(keyboard)
            .build();
    }

    private static InlineKeyboardButton enterAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("Ввести адресу :multiple_houses:"));
        button.setCallbackData("/enter_address");
        return button;
    }

    private static InlineKeyboardButton changeAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("Змінити адресу :arrows_counterclockwise:"));
        button.setCallbackData("/change_address");
        return button;
    }

    private static InlineKeyboardButton showAddressButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("Показати збережену адресу :eyes:"));
        button.setCallbackData("/show_address");
        return button;
    }

    private static InlineKeyboardButton showScheduleButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("Отримати графік на сьогодні :bulb:"));
        button.setCallbackData("/today_schedule");
        return button;
    }


}
