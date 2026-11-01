package io.github.phs314.quicktts.bot.speech.adapter.out.memory;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.BotVoiceState;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class BotVoiceStateMemoryAdapterTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final GuildId OTHER_GUILD = new GuildId(9L);

    private final BotVoiceStateMemoryAdapter repository = new BotVoiceStateMemoryAdapter();

    @Test
    void 처음_묻는_디스코드_서버는_채널이_없고_바꾼_상태를_기억한다() {
        VoiceChannel general = new VoiceChannel(GUILD, new ChannelId(2L));
        repository.withLockedState(GUILD, state -> {
            assertThat(state.channel()).isEmpty();
            state.join(general, channel -> false);
        });

        repository.withLockedState(GUILD, state -> assertThat(state.channel()).contains(general));
    }

    @Test
    void 채널이_없는_상태는_기억하지_않는다() {
        VoiceChannel general = new VoiceChannel(GUILD, new ChannelId(2L));
        repository.withLockedState(GUILD, state -> state.join(general, channel -> false));
        assertThat(repository.remembers(GUILD)).isTrue();

        repository.withLockedState(GUILD, BotVoiceState::clear);
        repository.withLockedState(OTHER_GUILD, state -> { });

        assertThat(repository.remembers(GUILD)).isFalse();
        assertThat(repository.remembers(OTHER_GUILD)).isFalse();
    }

    @Test
    void 기다리는_사이에_앞_작업이_상태를_지워도_새_상태에서_이어서_한다() throws Exception {
        VoiceChannel general = new VoiceChannel(GUILD, new ChannelId(2L));
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> repository.withLockedState(GUILD, state -> {
            holding.countDown();
            await(release);
        }));
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Void> waiting = CompletableFuture.runAsync(
                () -> repository.withLockedState(GUILD, state -> state.join(general, channel -> false)));
        release.countDown();
        first.get(5, TimeUnit.SECONDS);
        waiting.get(5, TimeUnit.SECONDS);

        repository.withLockedState(GUILD, state -> assertThat(state.channel()).contains(general));
    }

    @Test
    void 같은_디스코드_서버는_앞_작업이_끝날_때까지_기다리고_다른_디스코드_서버는_기다리지_않는다() throws Exception {
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> repository.withLockedState(GUILD, state -> {
            holding.countDown();
            await(release);
        }));
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();

        AtomicBoolean sameGuildRan = new AtomicBoolean();
        CompletableFuture<Void> sameGuild = CompletableFuture.runAsync(
                () -> repository.withLockedState(GUILD, state -> sameGuildRan.set(true)));
        CompletableFuture.runAsync(() -> repository.withLockedState(OTHER_GUILD, state -> { }))
                .get(5, TimeUnit.SECONDS);
        assertThat(sameGuildRan).isFalse();

        release.countDown();
        first.get(5, TimeUnit.SECONDS);
        sameGuild.get(5, TimeUnit.SECONDS);
        assertThat(sameGuildRan).isTrue();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
