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
}
