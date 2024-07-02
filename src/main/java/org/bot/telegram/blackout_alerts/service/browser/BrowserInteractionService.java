package org.bot.telegram.blackout_alerts.service.browser;

import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.KYIV;
import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.acquireWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.releaseWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_GROUP;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_SCHEDULE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.closeModal;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.dtekPageIsReady;
import static org.bot.telegram.blackout_alerts.util.UserSessionUtil.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Setter
@Getter
public class BrowserInteractionService {

    private WebDriver driver;

    private WebDriverWait pageAwait;

    private WebDriverWait autocompleteAwait;

    public String getShutDownSchedule(UserSession userSession) {
        acquireWebDriverWithAwaits(this, userSession.getUserCity());
        awaitForDtekPage();
        closeModal(driver);

        String schedule;
        try {
            if (!userSession.getUserCity().equals(KYIV)) {
                fillCityInput(userSession);
            }

            fillStreetInput(userSession);
            fillHouseInput(userSession);

            setShutdownGroupByJS(userSession);
            schedule = getScheduleByJS();
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return schedule;
    }

    private void awaitForDtekPage() {
        try {
            pageAwait.until(dtekPageIsReady());
        } catch (WebDriverException e) {
            driver.navigate().refresh();
            pageAwait.until(dtekPageIsReady());
        }
    }

    private void fillCityInput(UserSession userSession) {
        String userCity = userSession.getUserCity();
        WebElement input = driver.findElement(By.xpath(XPATH_CITY_INPUT));
        fillInput(input, userCity);

        String cityAutocomplete;
        try {
            cityAutocomplete = getAutocompleteInput(input, XPATH_CITY_AUTOCOMPLETE, userCity);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            throw new InvalidAddressException(AddressField.CITY, userCity);
        }

        if (!userCity.equals(cityAutocomplete)) {
            log.warn("User city {} does not match autocomplete city {}", userCity, cityAutocomplete);
            userSession.setUserCity(cityAutocomplete);
        }
    }

    private void fillStreetInput(UserSession userSession) {
        String userStreet = userSession.getUserStreet();
        WebElement input = driver.findElement(By.xpath(XPATH_STREET_INPUT));
        fillInput(input, userStreet);

        String xpath = String.format(XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT, userStreet.toLowerCase());
        String streetAutocomplete = getStreetStrictAutocompleteInput(input, xpath);

        if (streetAutocomplete == null) {
            try {
                String street = parseStreet(userStreet);
                fillInput(input, street);
                streetAutocomplete = getAutocompleteInput(input, XPATH_STREET_AUTOCOMPLETE, street);
            } catch (IllegalArgumentException e) {
                log.warn(e.getMessage());
                throw new InvalidAddressException(AddressField.STREET, userStreet);
            }
        }

        if (!userStreet.equals(streetAutocomplete)) {
            log.warn("User street {} does not match autocomplete street {}", userStreet, streetAutocomplete);
            userSession.setUserStreet(streetAutocomplete);
        }
    }

    private String getStreetStrictAutocompleteInput(WebElement input, String xpath) {
        String autocompleteValue = null;
        try {
            WebElement autocompleteElement = autocompleteAwait.until(visibilityOfElementLocated(By.xpath(xpath)));
            autocompleteElement.click();
            autocompleteValue = input.getAttribute("value");
        } catch (WebDriverException e) {
            log.warn("Failed to get strict street autocomplete");
        }

        return autocompleteValue;
    }

    private void fillHouseInput(UserSession userSession) {
        String userHouse = userSession.getUserHouse();
        WebElement input = driver.findElement(By.xpath(XPATH_HOUSE_INPUT));
        fillInput(input, userHouse);

        String houseAutocomplete;
        try {
            houseAutocomplete = getAutocompleteInput(input, XPATH_HOUSE_AUTOCOMPLETE, userHouse);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            throw new InvalidAddressException(AddressField.HOUSE, userHouse);
        }

        if (!userHouse.equals(houseAutocomplete)) {
            log.warn("User house {} does not match autocomplete house {}", userHouse, houseAutocomplete);
            userSession.setUserHouse(houseAutocomplete);
        }
    }

    private void fillInput(WebElement input, String value) {
        input.clear();
        Actions actions = new Actions(driver)
            .click(input);

        for (int i = 0; i < value.length(); i++) {
            actions.sendKeys(value.subSequence(i, i + 1));
            actions.pause(Duration.ofMillis(200));
        }
        actions.pause(Duration.ofMillis(500));
        actions.perform();
    }

    private String getAutocompleteInput(WebElement input, String autocompleteXpath, String value) {
        StringBuilder modValue = new StringBuilder(value);

        String autocompleteValue = null;
        do {
            try {
                WebElement autocompleteElement = autocompleteAwait.until(
                    visibilityOfElementLocated(By.xpath(autocompleteXpath)));
                autocompleteElement.click();
                autocompleteValue = input.getAttribute("value");
            } catch (WebDriverException e) {
                log.warn("Failed to autocomplete {}", modValue);

                if (modValue.length() > 3) {
                    modValue.deleteCharAt(modValue.length() - 1);
                    log.info("Trying to autocomplete {}", modValue);

                    fillInput(input, modValue.toString());
                }
            }
        } while (modValue.length() > 3 && autocompleteValue == null);

        if (autocompleteValue == null) {
            throw new IllegalArgumentException("Failed to autocomplete " + modValue);
        }

        return autocompleteValue;
    }

    private void setShutdownGroupByJS(UserSession userSession) {
        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_GROUP);
        userSession.setShutdownGroup(Byte.parseByte(json));
    }

    private String getScheduleByJS() {
        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_SCHEDULE);
        return json.trim();
    }
}
