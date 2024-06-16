package org.bot.telegram.blackout_alerts.service;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.Duration;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
public class BrowserInteractionService {

    private static final String DTEK_URL = "https://www.dtek-krem.com.ua/ua/shutdowns";

    private static final String XPATH_INPUT_CITY = "//form[@id='discon_form']//input[@id='city']";
    private static final String XPATH_CITY_AUTOCOMPLETE = "//div[contains(@id,'cityautocomplete-list')]";

    private static final String XPATH_INPUT_STREET = "//form[@id='discon_form']//input[@id='street']";
    private static final String XPATH_STREET_AUTOCOMPLETE = "//div[contains(@id,'streetautocomplete-list')]";

    private static final String XPATH_INPUT_HOUSE = "//form[@id='discon_form']//input[@id='house_num']";
    private static final String XPATH_HOUSE_AUTOCOMPLETE = "//div[contains(@id,'house_numautocomplete-list')]";

    private static final String XPATH_GROUP = "//div[@id='group-name']";

    private static final String JS_GET_SCHEDULE = "return JSON.stringify(DisconSchedule.preset[\"data\"]);";

    public ShutDownSchedule getShutDownSchedule(UserSession userSession) {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.navigate().to(DTEK_URL);

        WebDriverWait await = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement inputCity = await.until(visibilityOfElementLocated(By.xpath(XPATH_INPUT_CITY)));
        inputCity.click();
        inputCity.sendKeys(userSession.getUserCity());

        WebElement autocompleteCity = await.until(visibilityOfElementLocated(By.xpath(XPATH_CITY_AUTOCOMPLETE)));
        autocompleteCity.click();

        WebElement inputStreet = await.until(visibilityOfElementLocated(By.xpath(XPATH_INPUT_STREET)));
        inputStreet.click();
        inputStreet.sendKeys(userSession.getUserStreet());

        WebElement autocompleteStreet = await.until(visibilityOfElementLocated(By.xpath(XPATH_STREET_AUTOCOMPLETE)));
        autocompleteStreet.click();

        WebElement inputHouse = await.until(visibilityOfElementLocated(By.xpath(XPATH_INPUT_HOUSE)));
        inputHouse.click();
        inputHouse.sendKeys(userSession.getUserHouse());

        WebElement autocompleteHouse = await.until(visibilityOfElementLocated(By.xpath(XPATH_HOUSE_AUTOCOMPLETE)));
        autocompleteHouse.click();

        WebElement groupElement = await.until(visibilityOfElementLocated(By.xpath(XPATH_GROUP)));
        String group = groupElement.getText();
        byte groupNumber = Byte.parseByte(String.valueOf(group.charAt(group.length() - 1)));

        userSession.setShutdownGroup(groupNumber);

        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_SCHEDULE);
        json = json.trim();

        TypeToken<ShutDownSchedule> token = new TypeToken<>() {};
        return new Gson().fromJson(json, token.getType());
    }
}
