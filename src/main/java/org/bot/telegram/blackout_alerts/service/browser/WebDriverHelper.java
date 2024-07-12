package org.bot.telegram.blackout_alerts.service.browser;

import static org.bot.telegram.blackout_alerts.util.AddressUtil.isKyiv;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.util.BrowserPageUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class WebDriverHelper {

    private WebDriverHelper() {
    }

    protected static void acquireWebDriverWithAwaits(BrowserInteractionService service, String city) {
        WebDriver webDriver = getDriver();

        if (isKyiv(city)) {
            webDriver.navigate().to(BrowserPageUtil.DTEK_KYIV_URL);
        } else {
            webDriver.navigate().to(BrowserPageUtil.DTEK_REGIONS_URL);
        }
        log.info("Driver`s current url {}", webDriver.getCurrentUrl());

        service.setDriver(webDriver);
        service.setPageAwait(getPageAwait(webDriver));
        service.setAutocompleteAwait(getAutocompleteAwait(webDriver));
    }

    protected static void releaseWebDriverWithAwaits(BrowserInteractionService service) {
        service.getDriver().quit();
        service.setDriver(null);
        service.setPageAwait(null);
        service.setAutocompleteAwait(null);
    }

    private static WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return WebDriverManager.chromedriver().capabilities(options).create();
    }

    private static WebDriverWait getPageAwait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private static WebDriverWait getAutocompleteAwait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(5), Duration.ofSeconds(1));
    }
}
