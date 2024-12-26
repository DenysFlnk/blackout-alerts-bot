package org.bot.telegram.blackout_alerts.model.json;

import com.google.gson.annotations.SerializedName;

public class ShutDownSchedule {

    private static final String GROUP1_1 = "1.1";
    private static final String GROUP1_2 = "1.2";
    private static final String GROUP2_1 = "2.1";
    private static final String GROUP2_2 = "2.2";
    private static final String GROUP3_1 = "3.1";
    private static final String GROUP3_2 = "3.2";
    private static final String GROUP4_1 = "4.1";
    private static final String GROUP4_2 = "4.2";
    private static final String GROUP5_1 = "5.1";
    private static final String GROUP5_2 = "5.2";
    private static final String GROUP6_1 = "6.1";
    private static final String GROUP6_2 = "6.2";

    public static final String[] groups = {GROUP1_1, GROUP1_2, GROUP2_1, GROUP2_2, GROUP3_1, GROUP3_2,
        GROUP4_1, GROUP4_2, GROUP5_1, GROUP5_2, GROUP6_1, GROUP6_2};

    @SerializedName(GROUP1_1)
    Group group1_1;
    @SerializedName(GROUP1_2)
    Group group1_2;
    @SerializedName(GROUP2_1)
    Group group2_1;
    @SerializedName(GROUP2_2)
    Group group2_2;
    @SerializedName(GROUP3_1)
    Group group3_1;
    @SerializedName(GROUP3_2)
    Group group3_2;
    @SerializedName(GROUP4_1)
    Group group4_1;
    @SerializedName(GROUP4_2)
    Group group4_2;
    @SerializedName(GROUP5_1)
    Group group5_1;
    @SerializedName(GROUP5_2)
    Group group5_2;
    @SerializedName(GROUP6_1)
    Group group6_1;
    @SerializedName(GROUP6_2)
    Group group6_2;

    public Group getGroup(String groupNumber) {
        switch (groupNumber) {
            case GROUP1_1 -> {
                return group1_1;
            }
            case GROUP1_2 -> {
                return group1_2;
            }
            case GROUP2_1 -> {
                return group2_1;
            }
            case GROUP2_2 -> {
                return group2_2;
            }
            case GROUP3_1 -> {
                return group3_1;
            }
            case GROUP3_2 -> {
                return group3_2;
            }
            case GROUP4_1 -> {
                return group4_1;
            }
            case GROUP4_2 -> {
                return group4_2;
            }
            case GROUP5_1 -> {
                return group5_1;
            }
            case GROUP5_2 -> {
                return group5_2;
            }
            case GROUP6_1 -> {
                return group6_1;
            }
            case GROUP6_2 -> {
                return group6_2;
            }
            default -> throw new IllegalArgumentException("Invalid group number - " + groupNumber);
        }
    }
}
