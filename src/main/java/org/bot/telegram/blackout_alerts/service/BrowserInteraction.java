package org.bot.telegram.blackout_alerts.service;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.time.Duration;
import org.bot.telegram.blackout_alerts.model.json.ShutDownSchedule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BrowserInteraction {

    private static final String DTEK_URL = "https://www.dtek-krem.com.ua/ua/shutdowns";

    private static final String XPATH_INPUT_CITY = "//form[@id='discon_form']//input[@name='city']";
    private static final String XPATH_CITY_AUTOCOMPLETE = "//div[contains(@id,'cityautocomplete-list')]";

    private static final String XPATH_INPUT_STREET = "//form[@id='discon_form']//input[@name='street']";
    private static final String XPATH_STREET_AUTOCOMPLETE = "//div[contains(@id,'streetautocomplete-list')]";

    private static final String JS_GET_SCHEDULE = "return JSON.stringify(DisconSchedule.preset[\"data\"]);";

    public void getShutDownSchedule(String[] inputs) {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.navigate().to(DTEK_URL);

        String city = inputs[0];
        String street = inputs[1];

        WebDriverWait await = new WebDriverWait(driver, Duration.ofSeconds(30));

        WebElement inputCity = await.until(visibilityOfElementLocated(By.xpath(XPATH_INPUT_CITY)));
        inputCity.click();
        inputCity.sendKeys(city);

        WebElement autocompleteCity = await.until(visibilityOfElementLocated(By.xpath(XPATH_CITY_AUTOCOMPLETE)));
        autocompleteCity.click();

        WebElement inputStreet = await.until(visibilityOfElementLocated(By.xpath(XPATH_INPUT_STREET)));
        inputStreet.click();
        inputStreet.sendKeys(street);

        WebElement autocompleteStreet = await.until(visibilityOfElementLocated(By.xpath(XPATH_STREET_AUTOCOMPLETE)));
        autocompleteStreet.click();


        String json = (String) ((JavascriptExecutor) driver).executeScript(JS_GET_SCHEDULE);
        json = json.trim();

        TypeToken<ShutDownSchedule> token = new TypeToken<>() {};
        ShutDownSchedule schedule = new Gson().fromJson(json, token.getType());
    }
}
