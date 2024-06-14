package org.bot.telegram.blackout_alerts.model.json;

import com.google.gson.annotations.SerializedName;

public class ShutDownSchedule {

    @SerializedName("1")
    Group group1;
    @SerializedName("2")
    Group group2;
    @SerializedName("3")
    Group group3;
    @SerializedName("4")
    Group group4;

    public Group getGroup(int groupNumber) {
        switch (groupNumber) {
            case 1 -> {
                return group1;
            }
            case 2 -> {
                return group2;
            }
            case 3 -> {
                return group3;
            }
            case 4 -> {
                return group4;
            }
            default -> throw new IllegalArgumentException("Invalid group number");
        }
    }
}
