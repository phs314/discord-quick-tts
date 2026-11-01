package io.github.phs314.quicktts.bot.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * {@code @Cacheable} 을 켠다. 캐시 이름과 크기는 application.yml 의 {@code spring.cache} 에 있다.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
