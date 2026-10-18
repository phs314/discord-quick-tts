package io.github.phs314.quicktts.bot.speech.adapter.out.memory;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class BotSeatMemoryAdapterTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final GuildId OTHER_GUILD = new GuildId(9L);

    private final BotSeatMemoryAdapter repository = new BotSeatMemoryAdapter();

    @Test
    void 처음_묻는_디스코드_서버는_빈_자리이고_바꾼_자리를_기억한다() {
        VoiceChannel general = new VoiceChannel(GUILD, 2L);
        repository.withLockedSeat(GUILD, seat -> {
            assertThat(seat.channel()).isEmpty();
            seat.sitIn(general, channel -> false);
        });

        repository.withLockedSeat(GUILD, seat -> assertThat(seat.channel()).contains(general));
    }

    @Test
    void 같은_디스코드_서버는_앞_작업이_끝날_때까지_기다리고_다른_디스코드_서버는_기다리지_않는다() throws Exception {
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> repository.withLockedSeat(GUILD, seat -> {
            holding.countDown();
            await(release);
        }));
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();

        AtomicBoolean sameGuildRan = new AtomicBoolean();
        CompletableFuture<Void> sameGuild = CompletableFuture.runAsync(
                () -> repository.withLockedSeat(GUILD, seat -> sameGuildRan.set(true)));
        CompletableFuture.runAsync(() -> repository.withLockedSeat(OTHER_GUILD, seat -> { }))
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
