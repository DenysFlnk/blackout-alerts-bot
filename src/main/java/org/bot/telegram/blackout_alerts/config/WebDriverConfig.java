package org.bot.telegram.blackout_alerts.config;

import static org.bot.telegram.blackout_alerts.util.BrowserPageUtil.DTEK_URL;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver webDriver() {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.navigate().to(DTEK_URL);
        return driver;
    }

    @Bean
    public WebDriverWait pageAwait() {
        return new WebDriverWait(webDriver(), Duration.ofSeconds(15));
    }

    @Bean
    public WebDriverWait autocompleteAwait() {
        return new WebDriverWait(webDriver(), Duration.ofSeconds(3), Duration.ofSeconds(1));
    }
}
