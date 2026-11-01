package io.github.phs314.quicktts.bot.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서비스와 어댑터가 함께 쓰는 시계. 테스트에서는 고정된 시계로 바꿔 끼운다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
