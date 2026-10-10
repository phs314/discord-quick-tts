package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FollowBotMoveServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final VoiceChannel GENERAL = new VoiceChannel(GUILD, 2L);
    private static final VoiceChannel GAME = new VoiceChannel(GUILD, 3L);

    private final RecordingSpeechPlayer player = new RecordingSpeechPlayer();
    private final FakeBotSeatPort seats = new FakeBotSeatPort();
    private final FollowBotMoveService service = new FollowBotMoveService(seats, player);

    @Test
    void 관리자가_봇을_다른_채널로_옮기면_자리도_따라간다() {
        sitIn(GENERAL);

        service.followBotMove(GENERAL, Optional.of(GAME));

        assertThat(seats.channelOf(GUILD)).contains(GAME);
        assertThat(player.actions).isEmpty();
    }

    @Test
    void 관리자가_봇의_연결을_끊으면_자리를_비우고_남은_문장을_버린다() {
        sitIn(GENERAL);

        service.followBotMove(GENERAL, Optional.empty());

        assertThat(seats.channelOf(GUILD)).isEmpty();
        assertThat(player.actions).containsExactly("leave");
    }

    @Test
    void 봇_서버가_이미_다른_채널로_자리를_옮겼으면_늦게_온_소식은_무시한다() {
        sitIn(GAME);

        service.followBotMove(GENERAL, Optional.empty());

        assertThat(seats.channelOf(GUILD)).contains(GAME);
        assertThat(player.actions).isEmpty();
    }

    private void sitIn(VoiceChannel channel) {
        seats.withSeat(GUILD, seat -> seat.sitIn(channel, other -> false));
    }
}
