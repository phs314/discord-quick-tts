package io.github.phs314.quicktts.bot.speech.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class BotSeatTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final VoiceChannel GENERAL = new VoiceChannel(GUILD, 2L);
    private static final VoiceChannel GAME = new VoiceChannel(GUILD, 3L);
    private static final Predicate<VoiceChannel> NOBODY = channel -> false;

    private final BotSeat seat = BotSeat.empty(GUILD);

    @Test
    void 빈_자리는_어느_채널이든_앉을_수_있다() {
        seat.sitIn(GENERAL, NOBODY);

        assertThat(seat.channel()).contains(GENERAL);
    }

    @Test
    void 앉은_채널에_사람이_있으면_다른_채널은_앉을_수_없다() {
        seat.sitIn(GAME, NOBODY);
        Predicate<VoiceChannel> peopleInGame = Set.of(GAME)::contains;

        assertThat(seat.isFreeFor(GENERAL, peopleInGame)).isFalse();
        assertThatThrownBy(() -> seat.sitIn(GENERAL, peopleInGame)).isInstanceOf(IllegalStateException.class);
        assertThat(seat.channel()).contains(GAME);
    }

    @Test
    void 앉은_채널이면_사람이_있어도_다시_앉을_수_있다() {
        seat.sitIn(GENERAL, NOBODY);

        assertThat(seat.isFreeFor(GENERAL, Set.of(GENERAL)::contains)).isTrue();
    }

    @Test
    void 앉은_채널에_사람이_없으면_다른_채널로_옮길_수_있다() {
        seat.sitIn(GAME, NOBODY);

        seat.sitIn(GENERAL, Set.of(GENERAL)::contains);

        assertThat(seat.channel()).contains(GENERAL);
    }

    @Test
    void 다른_디스코드_서버의_채널에는_앉을_수_없다() {
        assertThatThrownBy(() -> seat.sitIn(new VoiceChannel(new GuildId(9L), 2L), NOBODY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 앉은_채널이_비면_자리를_비운다() {
        seat.sitIn(GENERAL, NOBODY);

        assertThat(seat.vacateIfEmpty(Set.of(GENERAL)::contains)).isFalse();
        assertThat(seat.vacateIfEmpty(NOBODY)).isTrue();
        assertThat(seat.channel()).isEmpty();
        assertThat(seat.vacateIfEmpty(NOBODY)).isFalse();
    }

    @Test
    void 봇이_옮겨지면_앉은_채널일_때만_따라간다() {
        seat.sitIn(GENERAL, NOBODY);

        assertThat(seat.followBotMove(GAME, Optional.empty())).isFalse();
        assertThat(seat.channel()).contains(GENERAL);

        assertThat(seat.followBotMove(GENERAL, Optional.of(GAME))).isFalse();
        assertThat(seat.channel()).contains(GAME);

        assertThat(seat.followBotMove(GAME, Optional.empty())).isTrue();
        assertThat(seat.channel()).isEmpty();
    }

    @Test
    void 같은_자리인지는_디스코드_서버로만_가린다() {
        BotSeat sameGuild = BotSeat.empty(GUILD);
        sameGuild.sitIn(GAME, NOBODY);

        assertThat(sameGuild).isEqualTo(seat).hasSameHashCodeAs(seat);
        assertThat(BotSeat.empty(new GuildId(9L))).isNotEqualTo(seat);
    }
}
