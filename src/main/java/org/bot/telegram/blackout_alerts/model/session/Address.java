package org.bot.telegram.blackout_alerts.model.session;

import lombok.Data;

@Data
public class Address {

    private String city;

    private String street;

    private String house;

    private String shutdownGroup;
}
