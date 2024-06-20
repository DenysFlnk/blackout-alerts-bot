package org.bot.telegram.blackout_alerts.service.browser;

import java.time.Duration;
import org.bot.telegram.blackout_alerts.util.BrowserPageUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverHelper {

    public static final String KYIV = "Київ";

    private WebDriverHelper() {
    }

    protected static void acquireWebDriverWithAwaits(BrowserInteractionService service, String city) {
        WebDriver webDriver = getDriver();
        webDriver.manage().window().maximize();

        if (city.equals(KYIV)) {
            webDriver.navigate().to(BrowserPageUtil.DTEK_KYIV_URL);
        } else {
            webDriver.navigate().to(BrowserPageUtil.DTEK_REGIONS_URL);
        }

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

        return new ChromeDriver(options);
    }

    private static WebDriverWait getPageAwait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private static WebDriverWait getAutocompleteAwait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(3), Duration.ofSeconds(1));
    }
}
