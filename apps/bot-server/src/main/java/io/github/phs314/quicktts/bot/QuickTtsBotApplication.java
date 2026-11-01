package io.github.phs314.quicktts.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * 사람은 아이디·비밀번호가 아니라 기기 토큰으로 알아내므로(ADR 0031) 스프링 시큐리티의 기본 사용자는 만들지 않는다.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class QuickTtsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickTtsBotApplication.class, args);
    }
}
