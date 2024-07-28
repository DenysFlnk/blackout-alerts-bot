package org.bot.telegram.blackout_alerts.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class KeyboardBuilder {

    private static final InlineKeyboardButton enterAddressButton = enterAddressButton();
    private static final InlineKeyboardButton changeAddressButton = changeAddressButton();
    private static final InlineKeyboardButton showAddressButton = showAddressButton();
    private static final InlineKeyboardButton showScheduleButton = showScheduleButton();
    private static final InlineKeyboardButton showWeekScheduleButton = showWeekScheduleButton();
    private static final InlineKeyboardButton checkShutdownStatusButton = checkShutdownStatusButton();
    private static final InlineKeyboardButton returnToMenuButton = returnToMenuButton();
    private static final InlineKeyboardButton answerToUserButton = answerToUserButton();
    private static final InlineKeyboardButton manageAlertSubscriptionButton = manageAlertSubscriptionButton();
    private static final InlineKeyboardButton createAlertSubscriptionButton = createAlertSubscriptionButton();
    private static final InlineKeyboardButton deleteAlertSubscriptionButton = deleteAlertSubscriptionButton();

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

    public KeyboardBuilder addReturnToMenuButton() {
        keyboard.add(Collections.singletonList(returnToMenuButton));
        return this;
    }

    public KeyboardBuilder addAddressOptions(AddressField field, List<String> options) {
        String callbackFormat = "%s %s";

        List<InlineKeyboardButton> row = new ArrayList<>();
        int maxSize = options.size() > 12 ? 3 : 2;
        for (String option : options) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(option);
            button.setCallbackData(String.format(callbackFormat, field, option));

            if (row.size() == maxSize) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
            row.add(button);
        }

        return this;
    }

    public KeyboardBuilder addAnswerToUserButton() {
        keyboard.add(Collections.singletonList(answerToUserButton));
        return this;
    }

    public KeyboardBuilder addManageAlertSubscriptionButton() {
        keyboard.add(Collections.singletonList(manageAlertSubscriptionButton));
        return this;
    }

    public KeyboardBuilder addCreateAlertSubscriptionButton() {
        keyboard.add(Collections.singletonList(createAlertSubscriptionButton));
        return this;
    }

    public KeyboardBuilder addDeleteAlertSubscriptionButton() {
        keyboard.add(Collections.singletonList(deleteAlertSubscriptionButton));
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

    private static InlineKeyboardButton returnToMenuButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Повернутись до меню \uD83D\uDCF2");
        button.setCallbackData("/menu");
        return button;
    }

    private static InlineKeyboardButton answerToUserButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Відповісти на питання ❔");
        button.setCallbackData("/answer_to_user");
        return button;
    }

    private static InlineKeyboardButton manageAlertSubscriptionButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Керувати сповіщеннями про відключення \uD83D\uDCEC");
        button.setCallbackData("/manage_subscription");
        return button;
    }

    private static InlineKeyboardButton createAlertSubscriptionButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Підписатись на сповіщення  📳");
        button.setCallbackData("/alert_subscription");
        return button;
    }

    private static InlineKeyboardButton deleteAlertSubscriptionButton() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Відписатись від сповіщення 📵");
        button.setCallbackData("/remove_alert_subscription");
        return button;
    }
}
