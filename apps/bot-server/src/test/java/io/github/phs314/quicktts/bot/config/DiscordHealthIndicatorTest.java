package io.github.phs314.quicktts.bot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

class DiscordHealthIndicatorTest {

    private final ShardManager shardManager = mock(ShardManager.class);
    private final DiscordHealthIndicator indicator = new DiscordHealthIndicator(shardManager);

    @Test
    void 모든_샤드가_붙어_있으면_UP_이고_샤드마다_상태를_보여_준다() {
        List<JDA> shards = List.of(shard(0, JDA.Status.CONNECTED), shard(1, JDA.Status.CONNECTED));
        when(shardManager.getShards()).thenReturn(shards);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("shards")).isEqualTo(Map.of(0, "CONNECTED", 1, "CONNECTED"));
    }

    @Test
    void 샤드_하나라도_끊기면_DOWN() {
        List<JDA> shards = List.of(shard(0, JDA.Status.CONNECTED), shard(1, JDA.Status.ATTEMPTING_TO_RECONNECT));
        when(shardManager.getShards()).thenReturn(shards);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void 아직_샤드가_없으면_DOWN() {
        when(shardManager.getShards()).thenReturn(List.of());

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    private static JDA shard(int id, JDA.Status status) {
        JDA shard = mock(JDA.class, RETURNS_DEEP_STUBS);
        when(shard.getShardInfo().getShardId()).thenReturn(id);
        when(shard.getStatus()).thenReturn(status);
        return shard;
    }
}
