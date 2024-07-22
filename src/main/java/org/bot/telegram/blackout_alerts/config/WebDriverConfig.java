package org.bot.telegram.blackout_alerts.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @PostConstruct
    private void locateWebDriver() {
        System.setProperty("webdriver.chrome.driver", "/bot/chromedriver");
    }
}
