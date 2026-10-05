package io.github.phs314.quicktts.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuickTtsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickTtsBotApplication.class, args);
    }
}
