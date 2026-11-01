package io.github.phs314.quicktts.bot.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 상태 확인({@code /actuator/health})에 디스코드 연결을 더한다. 모든 샤드가 게이트웨이에 붙어 있을 때만 UP 이다.
 * 샤드 하나라도 끊기면 그 샤드의 디스코드 서버에서는 봇이 말을 못 하기 때문이다.
 * JDA 는 끊기면 스스로 다시 붙으므로, 잠깐 DOWN 이 보이는 건 다시 붙는 중일 수 있다.
 */
@Component
@RequiredArgsConstructor
public class DiscordHealthIndicator implements HealthIndicator {

    private final ShardManager shardManager;

    @Override
    public Health health() {
        List<JDA> shards = shardManager.getShards();
        Map<Integer, String> statuses = new TreeMap<>();
        shards.forEach(shard -> statuses.put(shard.getShardInfo().getShardId(), String.valueOf(shard.getStatus())));
        boolean connected = !shards.isEmpty() && shards.stream().allMatch(shard -> shard.getStatus() == JDA.Status.CONNECTED);
        Health.Builder health = connected ? Health.up() : Health.down();
        return health.withDetail("shards", statuses)
                .withDetail("gatewayPingMillis", Math.round(shardManager.getAverageGatewayPing()))
                .build();
    }
}
