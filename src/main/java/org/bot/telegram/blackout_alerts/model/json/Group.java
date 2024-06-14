package org.bot.telegram.blackout_alerts.model.json;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Group {

    @SerializedName("1")
    TimeZone monday;
    @SerializedName("2")
    TimeZone tuesday;
    @SerializedName("3")
    TimeZone wednesday;
    @SerializedName("4")
    TimeZone thursday;
    @SerializedName("5")
    TimeZone friday;
    @SerializedName("6")
    TimeZone saturday;
    @SerializedName("7")
    TimeZone sunday;
}
