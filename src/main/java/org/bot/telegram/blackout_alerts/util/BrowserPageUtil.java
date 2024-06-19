package org.bot.telegram.blackout_alerts.util;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class BrowserPageUtil {

    private BrowserPageUtil() {
    }

    public static final String DTEK_URL = "https://www.dtek-krem.com.ua/ua/shutdowns";

    public static final String XPATH_CITY_INPUT = "//form[@id='discon_form']//input[@id='city']";
    public static final String XPATH_CITY_AUTOCOMPLETE = "//div[contains(@id,'cityautocomplete-list')]/div[1]";

    public static final String XPATH_STREET_INPUT = "//form[@id='discon_form']//input[@id='street']";
    public static final String XPATH_STREET_AUTOCOMPLETE = "//div[contains(@id,'streetautocomplete-list')]/div[1]";

    public static final String XPATH_HOUSE_INPUT = "//form[@id='discon_form']//input[@id='house_num']";
    public static final String XPATH_HOUSE_AUTOCOMPLETE = "//div[contains(@id,'house_numautocomplete-list')]/div[1]";

    public static final String XPATH_GROUP_CONTAINER = "//div[@id='group-name']";

    public static final String JS_GET_SCHEDULE = "return JSON.stringify(DisconSchedule.preset[\"data\"]);";

    public static byte parseGroupNumber(String groupName) {
        return Byte.parseByte(String.valueOf(groupName.charAt(groupName.length() - 1)));
    }

    public static ExpectedCondition<Boolean> dtekPageIsReady() {
        return and(
            visibilityOfElementLocated(By.xpath(XPATH_CITY_INPUT)),
            visibilityOfElementLocated(By.xpath(XPATH_STREET_INPUT)),
            visibilityOfElementLocated(By.xpath(XPATH_HOUSE_INPUT))
        );
    }
}
