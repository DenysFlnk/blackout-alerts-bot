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
    @SerializedName("5")
    Group group5;
    @SerializedName("6")
    Group group6;

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
            case 5 -> {
                return group5;
            }
            case 6 -> {
                return group6;
            }
            default -> throw new IllegalArgumentException("Invalid group number");
        }
    }
}
