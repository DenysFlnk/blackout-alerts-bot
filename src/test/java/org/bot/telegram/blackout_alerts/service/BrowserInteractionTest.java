package org.bot.telegram.blackout_alerts.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BrowserInteractionTest {

    @Test
    void getShutDownSchedule() {
        String[] input = new String[] {"Ірпінь", "Університетська"};
        BrowserInteraction browserInteraction = new BrowserInteraction();

        browserInteraction.getShutDownSchedule(input);
    }
}