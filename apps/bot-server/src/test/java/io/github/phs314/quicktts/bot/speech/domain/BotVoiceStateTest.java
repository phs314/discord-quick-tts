package io.github.phs314.quicktts.bot.speech.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class BotVoiceStateTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final VoiceChannel GENERAL = new VoiceChannel(GUILD, new ChannelId(2L));
    private static final VoiceChannel GAME = new VoiceChannel(GUILD, new ChannelId(3L));
    private static final Predicate<VoiceChannel> NOBODY = channel -> false;

    private final BotVoiceState state = BotVoiceState.empty(GUILD);

    @Test
    void 채널이_없으면_어느_채널이든_들어갈_수_있다() {
        state.join(GENERAL, NOBODY);

        assertThat(state.channel()).contains(GENERAL);
    }

    @Test
    void 있는_채널에_사람이_있으면_다른_채널에는_들어갈_수_없다() {
        state.join(GAME, NOBODY);
        Predicate<VoiceChannel> peopleInGame = Set.of(GAME)::contains;

        assertThat(state.canJoin(GENERAL, peopleInGame)).isFalse();
        assertThatThrownBy(() -> state.join(GENERAL, peopleInGame)).isInstanceOf(IllegalStateException.class);
        assertThat(state.channel()).contains(GAME);
    }

    @Test
    void 있는_채널이면_사람이_있어도_다시_들어갈_수_있다() {
        state.join(GENERAL, NOBODY);

        assertThat(state.canJoin(GENERAL, Set.of(GENERAL)::contains)).isTrue();
    }

    @Test
    void 있는_채널에_사람이_없으면_다른_채널로_옮길_수_있다() {
        state.join(GAME, NOBODY);

        state.join(GENERAL, Set.of(GENERAL)::contains);

        assertThat(state.channel()).contains(GENERAL);
    }

    @Test
    void 다른_디스코드_서버의_채널에는_들어갈_수_없다() {
        assertThatThrownBy(() -> state.join(new VoiceChannel(new GuildId(9L), new ChannelId(2L)), NOBODY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 있는_채널이_비면_나간다() {
        state.join(GENERAL, NOBODY);

        assertThat(state.leaveIfEmpty(Set.of(GENERAL)::contains)).isFalse();
        assertThat(state.leaveIfEmpty(NOBODY)).isTrue();
        assertThat(state.channel()).isEmpty();
        assertThat(state.leaveIfEmpty(NOBODY)).isFalse();
    }

    @Test
    void 사람이_있어도_채널을_지울_수_있다() {
        state.join(GENERAL, NOBODY);

        state.clear();

        assertThat(state.channel()).isEmpty();
        assertThat(state.canJoin(GAME, Set.of(GENERAL)::contains)).isTrue();
    }

    @Test
    void 봇이_옮겨지면_지금_채널일_때만_따라간다() {
        state.join(GENERAL, NOBODY);

        assertThat(state.applyUpdate(GAME, Optional.empty())).isFalse();
        assertThat(state.channel()).contains(GENERAL);

        assertThat(state.applyUpdate(GENERAL, Optional.of(GAME))).isFalse();
        assertThat(state.channel()).contains(GAME);

        assertThat(state.applyUpdate(GAME, Optional.empty())).isTrue();
        assertThat(state.channel()).isEmpty();
    }

    @Test
    void 같은_상태인지는_디스코드_서버로만_가린다() {
        BotVoiceState sameGuild = BotVoiceState.empty(GUILD);
        sameGuild.join(GAME, NOBODY);

        assertThat(sameGuild).isEqualTo(state).hasSameHashCodeAs(state);
        assertThat(BotVoiceState.empty(new GuildId(9L))).isNotEqualTo(state);
    }
}
