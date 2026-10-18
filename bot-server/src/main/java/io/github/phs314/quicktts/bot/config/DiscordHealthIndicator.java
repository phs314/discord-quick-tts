package io.github.phs314.quicktts.bot.config;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 상태 확인({@code /actuator/health})에 디스코드 연결을 더한다. 게이트웨이에 붙어 있을 때만 UP 이다.
 * JDA 는 끊기면 스스로 다시 붙으므로, 잠깐 DOWN 이 보이는 건 다시 붙는 중일 수 있다.
 */
@Component
@RequiredArgsConstructor
public class DiscordHealthIndicator implements HealthIndicator {

    private final JDA jda;

    @Override
    public Health health() {
        JDA.Status status = jda.getStatus();
        Health.Builder health = status == JDA.Status.CONNECTED ? Health.up() : Health.down();
        return health.withDetail("status", String.valueOf(status))
                .withDetail("gatewayPingMillis", jda.getGatewayPing())
                .build();
    }
}
