package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class InMemoryBotSeatRepositoryTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final GuildId OTHER_GUILD = new GuildId(9L);

    private final InMemoryBotSeatRepository repository = new InMemoryBotSeatRepository();

    @Test
    void 처음_묻는_디스코드_서버는_빈_자리이고_바꾼_자리를_기억한다() {
        VoiceChannel general = new VoiceChannel(GUILD, 2L);
        repository.withSeat(GUILD, seat -> {
            assertThat(seat.channel()).isEmpty();
            seat.sitIn(general, channel -> false);
        });

        repository.withSeat(GUILD, seat -> assertThat(seat.channel()).contains(general));
    }

    @Test
    void 같은_디스코드_서버는_앞_작업이_끝날_때까지_기다리고_다른_디스코드_서버는_기다리지_않는다() throws Exception {
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> repository.withSeat(GUILD, seat -> {
            holding.countDown();
            await(release);
        }));
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();

        AtomicBoolean sameGuildRan = new AtomicBoolean();
        CompletableFuture<Void> sameGuild = CompletableFuture.runAsync(
                () -> repository.withSeat(GUILD, seat -> sameGuildRan.set(true)));
        CompletableFuture.runAsync(() -> repository.withSeat(OTHER_GUILD, seat -> { }))
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
