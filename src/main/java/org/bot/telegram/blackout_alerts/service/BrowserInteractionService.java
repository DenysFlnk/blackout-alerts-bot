package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_SCHEDULE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_GROUP_CONTAINER;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.dtekPageIsReady;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.parseGroupNumber;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrowserInteractionService {

    private final WebDriver driver;

    private final WebDriverWait pageAwait;

    private final WebDriverWait autocompleteAwait;

    public BrowserInteractionService(WebDriver driver, @Qualifier("pageAwait") WebDriverWait pageAwait,
                                     @Qualifier("autocompleteAwait") WebDriverWait autocompleteAwait) {
        this.driver = driver;
        this.pageAwait = pageAwait;
        this.autocompleteAwait = autocompleteAwait;
    }

    public ShutDownSchedule getShutDownSchedule(UserSession userSession) {
        awaitForDtekPage();

        fillCityInput(userSession);
        fillStreetInput(userSession);
        fillHouseInput(userSession);

        setShutdownGroup(userSession);

        return getScheduleByJS();
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
            throw new RuntimeException("""
                Йой! Схоже, що не правильно введений населений пункт.
                Натисніть "Ввести адресу", щоб спробувати ще
                """); //TODO change exception
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

        String streetAutocomplete;
        try {
            streetAutocomplete = getAutocompleteInput(input, XPATH_STREET_AUTOCOMPLETE, userStreet);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("""
                Йой! Схоже, що не правильно введена вулиця.
                Натисніть "Ввести адресу", щоб спробувати ще
                """); //TODO change exception
        }

        if (!userStreet.equals(streetAutocomplete)) {
            log.warn("User street {} does not match autocomplete street {}", userStreet, streetAutocomplete);
            userSession.setUserStreet(streetAutocomplete);
        }
    }

    private void fillHouseInput(UserSession userSession) {
        String userHouse = userSession.getUserHouse();
        WebElement input = driver.findElement(By.xpath(XPATH_HOUSE_INPUT));
        fillInput(input, userHouse);

        String houseAutocomplete;
        try {
            houseAutocomplete = getAutocompleteInput(input, XPATH_HOUSE_AUTOCOMPLETE, userHouse);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("""
                Йой! Схоже, що не правильно введений номер будинку.
                Натисніть "Ввести адресу", щоб спробувати ще
                """); //TODO change exception
        }

        if (!userHouse.equals(houseAutocomplete)) {
            log.warn("User house {} does not match autocomplete house {}", userHouse, houseAutocomplete);
            userSession.setUserHouse(houseAutocomplete);
        }
    }

    private void fillInput(WebElement input, String value) {
        Actions actions = new Actions(driver)
            .click(input);

        for (int i = 0; i < value.length(); i++) {
            actions.sendKeys(value.subSequence(i, i + 1));
            actions.pause(Duration.ofMillis(200));
        }

        actions.perform();
    }

    private void setShutdownGroup(UserSession userSession) {
        WebElement groupElement = pageAwait.until(visibilityOfElementLocated(By.xpath(XPATH_GROUP_CONTAINER)));
        byte groupNumber = parseGroupNumber(groupElement.getText());
        userSession.setShutdownGroup(groupNumber);
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

                    input.clear();
                    fillInput(input, modValue.toString());
                }
            }
        } while (modValue.length() > 3 && autocompleteValue == null);

        if (autocompleteValue == null) {
            throw new IllegalArgumentException("Failed to autocomplete " + modValue);
        }

        return autocompleteValue;
    }

    private ShutDownSchedule getScheduleByJS() {
        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_SCHEDULE);
        json = json.trim();

        TypeToken<ShutDownSchedule> token = new TypeToken<>() {};
        return new Gson().fromJson(json, token.getType());
    }
}
