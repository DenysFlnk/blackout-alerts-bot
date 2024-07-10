package org.bot.telegram.blackout_alerts.service.browser;

import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.KYIV;
import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.acquireWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.releaseWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_GROUP;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_SCHEDULE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE_FORMAT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE_LIST;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.awaitAndCloseModal;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.dtekPageIsReady;
import static org.bot.telegram.blackout_alerts.util.UserSessionUtil.parseStreet;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
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

        String schedule;
        try {
            if (userSession.getUserCity().equals(KYIV)) {
                fillKyivInputs(userSession);
            } else {
                fillRegionInputs(userSession);
            }

            setShutdownGroupByJS(userSession);
            schedule = getScheduleByJS();
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return schedule;
    }

    private void awaitForDtekPage() {
        try {
            awaitAndCloseModal(autocompleteAwait);
            pageAwait.until(dtekPageIsReady());
        } catch (WebDriverException e) {
            log.warn("Failed to await dtek page. Trying again.");
            driver.navigate().refresh();
            awaitAndCloseModal(autocompleteAwait);
            pageAwait.until(dtekPageIsReady());
        }
    }

    private void fillKyivInputs(UserSession userSession) {
        fillStreetInput(userSession);
        fillHouseInput(userSession);
    }

    private void fillRegionInputs(UserSession userSession) {
        int autocompleteOptionsCount = getRegionCityOptionsCount(userSession);

        for (int i = 1; i <= autocompleteOptionsCount; i++) {
            try {
                fillRegionCityInput(userSession, i);
                fillStreetInput(userSession);
                fillHouseInput(userSession);
                break;
            } catch (InvalidAddressException e) {
                log.warn("Failed to fill inputs for city option {}", i);
                if (i == autocompleteOptionsCount) {
                    throw e;
                }
            }
        }

        String cityAutocomplete = driver.findElement(By.xpath(XPATH_CITY_INPUT)).getAttribute("value");
        if (!userSession.getUserCity().equals(cityAutocomplete)) {
            log.warn("User city {} does not match autocomplete city {}", userSession.getUserCity(), cityAutocomplete);
            userSession.setUserCity(cityAutocomplete);
        }
    }

    private int getRegionCityOptionsCount(UserSession userSession) {
        String userCity = userSession.getUserCity();
        WebElement input = driver.findElement(By.xpath(XPATH_CITY_INPUT));
        fillInput(input, userCity);

        int count = driver.findElements(By.xpath(XPATH_CITY_AUTOCOMPLETE_LIST)).size();

        if (count == 0) {
            throw new InvalidAddressException(AddressField.CITY, userCity);
        }

        return count;
    }

    private void fillRegionCityInput(UserSession userSession, int optionNumber) {
        String userCity = userSession.getUserCity();
        WebElement input = driver.findElement(By.xpath(XPATH_CITY_INPUT));
        fillInput(input, userCity);

        String autocompleteXpath = String.format(XPATH_CITY_AUTOCOMPLETE_FORMAT, optionNumber);
        String cityAutocomplete = getAutocompleteInput(input, autocompleteXpath, userCity);

        log.info("Chat id: {}, user city: {}, autocomplete: {}", userSession.getChatId(), userCity, cityAutocomplete);
    }

    private void fillStreetInput(UserSession userSession) {
        String userStreet = userSession.getUserStreet();
        WebElement input = pageAwait.until(elementToBeClickable(By.xpath(XPATH_STREET_INPUT)));
        fillInput(input, userStreet);

        String xpath = String.format(XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT, userStreet.toLowerCase());
        String streetAutocomplete = getStreetStrictAutocompleteInput(input, xpath);

        if (streetAutocomplete == null) {
            try {
                String street = parseStreet(userStreet);
                fillInput(input, street);
                streetAutocomplete = getAutocompleteInput(input, XPATH_STREET_AUTOCOMPLETE, street);
            } catch (IllegalArgumentException e) {
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
        WebElement input = pageAwait.until(elementToBeClickable(By.xpath(XPATH_HOUSE_INPUT)));
        fillInput(input, userHouse);

        String houseAutocomplete;
        try {
            houseAutocomplete = getAutocompleteInput(input, XPATH_HOUSE_AUTOCOMPLETE, userHouse);
        } catch (IllegalArgumentException e) {
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
            actions.pause(Duration.ofMillis(50));
        }
        actions.pause(Duration.ofMillis(200));
        actions.perform();
    }

    private String getAutocompleteInput(WebElement input, String autocompleteXpath, String value) {
        String autocompleteValue;
        try {
            WebElement autocompleteElement = autocompleteAwait.until(
                visibilityOfElementLocated(By.xpath(autocompleteXpath)));
            autocompleteElement.click();
            autocompleteValue = input.getAttribute("value");
        } catch (WebDriverException e) {
            log.warn("Failed to autocomplete {}", value);
            throw new IllegalArgumentException("Failed to autocomplete " + value);
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
