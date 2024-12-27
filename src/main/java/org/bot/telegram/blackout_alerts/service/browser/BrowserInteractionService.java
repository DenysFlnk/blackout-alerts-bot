package org.bot.telegram.blackout_alerts.service.browser;

import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.acquireWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.service.browser.WebDriverHelper.releaseWebDriverWithAwaits;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.isKyiv;
import static org.bot.telegram.blackout_alerts.util.AddressUtil.parseLastPartOfStreet;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_GROUP;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_GET_SCHEDULE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.JS_SCROLL_INTO_VIEW;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE_FORMAT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_AUTOCOMPLETE_LIST;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CITY_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_CLOSE_MODAL_BTN;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_COMMON_SCHEDULE_IMG;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_AUTOCOMPLETE_LIST;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_HOUSE_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_SCHEDULE_TABLE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_SHUTDOWN_STATUS;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.XPATH_STREET_INPUT;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.awaitForAny;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.dtekPageIsReady;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.elementIsVisible;
import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.modalIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.ShutdownStatusUnavailableException;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;
import org.bot.telegram.blackout_alerts.exception.address.InvalidAddressException;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
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

    public String getShutDownSchedule(UserSession session) {
        String userCity = session.getUserCity();
        acquireWebDriverWithAwaits(this, userCity);

        String schedule;
        try {
            awaitForDtekPage();

            if (isKyiv(userCity)) {
                fillKyivInputs(session);
            } else {
                fillRegionInputs(session);
            }

            setShutdownGroupByJS(session);
            schedule = getScheduleByJS();
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return schedule;
    }

    public ByteArrayInputStream getWeekShutdownScheduleScreenshot(UserSession session) {
        String userCity = session.getUserCity();
        acquireWebDriverWithAwaits(this, userCity);
        driver.manage().window().setSize(new Dimension(1024, 768));
        awaitForDtekPage();

        byte[] screenshotBytes;
        try {
            if (isKyiv(userCity)) {
                fillKyivInputs(session);
            } else {
                fillRegionInputs(session);
            }

            setShutdownGroupByJS(session);

            screenshotBytes = getScreenshotOfElementLocated(XPATH_SCHEDULE_TABLE);
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return new ByteArrayInputStream(screenshotBytes);
    }

    public byte[] getCommonShutdownScheduleScreenshot(String city) {
        acquireWebDriverWithAwaits(this, city);
        driver.manage().window().setSize(new Dimension(1024, 1080));
        awaitForDtekPage();

        byte[] screenshotBytes;
        try {
            screenshotBytes = getScreenshotOfElementLocated(XPATH_COMMON_SCHEDULE_IMG);
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return screenshotBytes;
    }

    public String getShutdownStatus(UserSession session) {
        String userCity = session.getUserCity();
        acquireWebDriverWithAwaits(this, userCity);
        awaitForDtekPage();

        String status;
        try {
            if (isKyiv(userCity)) {
                fillKyivInputs(session);
            } else {
                fillRegionInputs(session);
            }

            setShutdownGroupByJS(session);

            status = getShutdownStatus();
        } finally {
            releaseWebDriverWithAwaits(this);
        }

        return status;
    }

    private String getShutdownStatus() {
        String text;
        try {
            text = driver.findElement(By.xpath(XPATH_SHUTDOWN_STATUS)).getText();
            if (text.isEmpty()) {
                log.warn("Shutdown status text is empty");
                throw new ShutdownStatusUnavailableException();
            }
        } catch (WebDriverException e) {
            log.error("Error while getting shutdown status text", e);
            throw new ShutdownStatusUnavailableException();
        }

        return text;
    }

    private void awaitForDtekPage() {
        try {
            awaitForAny(pageAwait, dtekPageIsReady(), modalIsPresent());

            if (elementIsVisible(driver, XPATH_CLOSE_MODAL_BTN)) {
                log.debug("Modal window is present");
                driver.findElement(By.xpath(XPATH_CLOSE_MODAL_BTN)).click();
            }
        } catch (WebDriverException e) {
            log.warn("Failed to await dtek page. Trying again");
            driver.navigate().refresh();
            awaitForAny(pageAwait, dtekPageIsReady(), modalIsPresent());

            if (elementIsVisible(driver, XPATH_CLOSE_MODAL_BTN)) {
                log.debug("Modal window is present after refresh");
                driver.findElement(By.xpath(XPATH_CLOSE_MODAL_BTN)).click();
            }
        }
    }

    private void fillKyivInputs(UserSession session) {
        fillStreetInput(session);
        fillHouseInput(session);
    }

    private void fillRegionInputs(UserSession session) {
        try {
            fillRegionInputsWithAwareOfCityOptions(session);
        } catch (InvalidAddressException e) {
            log.warn("Chat id: {}. Failed to fill region inputs", session.getChatId());

            if (session.getUserCity().contains("(")) {
                String cityWithoutRegion = session.getUserCity().split(" ")[0];
                log.info("Chat id: {}. User city contains region, trying to fill inputs without it",
                    session.getChatId());

                session.setUserCity(cityWithoutRegion);
                fillRegionInputsWithAwareOfCityOptions(session);
            } else {
                throw e;
            }
        }
    }

    private void fillRegionInputsWithAwareOfCityOptions(UserSession session) {
        int autocompleteOptionsCount = getRegionCityOptionsCount(session);

        for (int i = 1; i <= autocompleteOptionsCount; i++) {
            try {
                fillRegionCityInput(session, i);
                fillStreetInput(session);
                fillHouseInput(session);
                break;
            } catch (InvalidAddressException e) {
                log.warn("Chat id: {}. Failed to fill inputs for city option {}", session.getChatId(), i);
                if (i == autocompleteOptionsCount) {
                    throw e;
                }
            }
        }

        String cityAutocomplete = driver.findElement(By.xpath(XPATH_CITY_INPUT)).getAttribute("value");
        if (!session.getUserCity().equals(cityAutocomplete)) {
            log.warn("Chat id: {}. User city {} does not match autocomplete city {}", session.getChatId(),
                session.getUserCity(), cityAutocomplete);
            session.setUserCity(cityAutocomplete);
        }
    }

    private int getRegionCityOptionsCount(UserSession session) {
        String userCity = session.getUserCity();
        WebElement input = driver.findElement(By.xpath(XPATH_CITY_INPUT));
        fillInput(input, userCity);

        int count = driver.findElements(By.xpath(XPATH_CITY_AUTOCOMPLETE_LIST)).size();

        // Workaround DTEK bug
        if (count == 0) {
            String cityWithoutLastLetter = userCity.substring(0, userCity.length() - 1);
            fillInput(input, cityWithoutLastLetter);
            count = driver.findElements(By.xpath(XPATH_CITY_AUTOCOMPLETE_LIST)).size();
            session.setUserCity(cityWithoutLastLetter);
        }

        if (count == 0) {
            session.setUserCity(userCity);
            throw new InvalidAddressException(AddressField.CITY, userCity);
        }

        return count;
    }

    private void fillRegionCityInput(UserSession session, int optionNumber) {
        String userCity = session.getUserCity();
        WebElement input = driver.findElement(By.xpath(XPATH_CITY_INPUT));
        fillInput(input, userCity);

        String autocompleteXpath = String.format(XPATH_CITY_AUTOCOMPLETE_FORMAT, optionNumber);
        String cityAutocomplete = getAutocompleteInput(input, autocompleteXpath, userCity);

        log.info("Chat id: {}. User city: {}, autocomplete: {}", session.getChatId(), userCity, cityAutocomplete);
    }

    private void fillStreetInput(UserSession session) {
        String userStreet = session.getUserStreet();
        WebElement input = pageAwait.until(elementToBeClickable(By.xpath(XPATH_STREET_INPUT)));
        fillInput(input, userStreet);

        String xpath = String.format(XPATH_STREET_AUTOCOMPLETE_STRICT_FORMAT, userStreet.toLowerCase());
        String streetAutocomplete = getStreetStrictAutocompleteInput(input, xpath);

        if (streetAutocomplete == null) {
            try {
                String street = parseLastPartOfStreet(userStreet);
                fillInput(input, street);
                streetAutocomplete = getAutocompleteInput(input, XPATH_STREET_AUTOCOMPLETE, street);
            } catch (IllegalArgumentException e) {
                throw new InvalidAddressException(AddressField.STREET, userStreet);
            }
        }

        if (!userStreet.equals(streetAutocomplete)) {
            log.warn("Chat id: {}. User street {} does not match autocomplete street {}", session.getChatId(),
                userStreet, streetAutocomplete);
            session.setUserStreet(streetAutocomplete);
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

    private void fillHouseInput(UserSession session) {
        String userHouse = session.getUserHouse();
        WebElement input = pageAwait.until(elementToBeClickable(By.xpath(XPATH_HOUSE_INPUT)));
        fillInput(input, userHouse);

        String houseAutocomplete;
        try {
            houseAutocomplete = getAutocompleteInput(input, XPATH_HOUSE_AUTOCOMPLETE, userHouse);
        } catch (IllegalArgumentException e) {
            String firstDigit = userHouse.substring(0, 1);
            fillInput(input, firstDigit);
            List<String> options = new ArrayList<>();
            driver.findElements(By.xpath(XPATH_HOUSE_AUTOCOMPLETE_LIST))
                .forEach(element -> options.add(element.getText()));
            throw new InvalidAddressException(AddressField.HOUSE, userHouse, options);
        }

        if (!userHouse.equals(houseAutocomplete)) {
            log.warn("Chat id: {}. User house {} does not match autocomplete house {}", session.getChatId(),
                userHouse, houseAutocomplete);
            session.setUserHouse(houseAutocomplete);
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

    private void setShutdownGroupByJS(UserSession session) {
        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_GROUP);
        session.setShutdownGroup(json.replace("\"", ""));
    }

    private String getScheduleByJS() {
        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_SCHEDULE);
        return json.trim();
    }

    private byte[] getScreenshotOfElementLocated(String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
        scrollIntoView(element);
        return element.getScreenshotAs(OutputType.BYTES);
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(JS_SCROLL_INTO_VIEW, element);
    }
}
