package io.github.phs314.quicktts.bot.guild.adapter.out.event;

import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistEventPort;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 스프링 애플리케이션 이벤트로 허용 목록 이벤트를 낸다. 서비스가 스프링을 모르도록 이 어댑터에만 둔다 (ADR 0026). */
@Component
@RequiredArgsConstructor
public class SpringGuildAllowlistEvent implements GuildAllowlistEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(GuildDisallowed event) {
        applicationEventPublisher.publishEvent(event);
    }
}
