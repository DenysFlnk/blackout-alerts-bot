package org.bot.telegram.blackout_alerts.util;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class BrowserPageUtil {

    private BrowserPageUtil() {
    }

    public static final String DTEK_KYIV_URL = "https://www.dtek-kem.com.ua/ua/shutdowns";
    public static final String DTEK_REGIONS_URL = "https://www.dtek-krem.com.ua/ua/shutdowns";

    public static final String XPATH_CITY_INPUT = "//form[@id='discon_form']//input[@id='city']";
    public static final String XPATH_CITY_AUTOCOMPLETE = "//div[contains(@id,'cityautocomplete-list')]/div[1]";

    public static final String XPATH_STREET_INPUT = "//form[@id='discon_form']//input[@id='street']";
    public static final String XPATH_STREET_AUTOCOMPLETE = "//div[contains(@id,'streetautocomplete-list')]/div[1]";
    public static final String XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT =
        "//div[contains(@id,'streetautocomplete-list')]/div[strong[text()='%s'] and not(text()[2])]";

    public static final String XPATH_HOUSE_INPUT = "//form[@id='discon_form']//input[@id='house_num']";
    public static final String XPATH_HOUSE_AUTOCOMPLETE =
        "//div[contains(@id,'house_numautocomplete-list')]/div[not(text())]";

    public static final String XPATH_CLOSE_MODAL_BTN = "//button[contains(@class,'modal__close')]";

    public static final String JS_GET_SCHEDULE = "return JSON.stringify(DisconSchedule.preset[\"data\"]);";
    public static final String JS_GET_GROUP = "return JSON.stringify(DisconSchedule.group);";

    public static void awaitAndCloseModal(WebDriverWait await) {
        try {
            await.until(visibilityOfElementLocated(By.xpath(XPATH_CLOSE_MODAL_BTN))).click();
            log.info("Found modal window");
        } catch (WebDriverException e) {
            log.info("Modal window not found");
        }
    }

    public static ExpectedCondition<Boolean> dtekPageIsReady() {
        return and(
            visibilityOfElementLocated(By.xpath(XPATH_STREET_INPUT)),
            visibilityOfElementLocated(By.xpath(XPATH_HOUSE_INPUT))
        );
    }
}
