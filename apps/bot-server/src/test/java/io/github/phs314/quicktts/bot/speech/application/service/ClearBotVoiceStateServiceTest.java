package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ClearBotVoiceStateCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeBotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import org.junit.jupiter.api.Test;

class ClearBotVoiceStateServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final VoiceChannel GENERAL = new VoiceChannel(GUILD, new ChannelId(2L));
    private static final VoiceChannel GAME = new VoiceChannel(GUILD, new ChannelId(3L));

    private final FakeBotVoiceStatePort states = new FakeBotVoiceStatePort();
    private final FakeSpeechPlayerPort player = new FakeSpeechPlayerPort();
    private final ClearBotVoiceStateService service = new ClearBotVoiceStateService(states, player);

    @Test
    void 봇이_디스코드_서버에서_나가면_채널에_사람이_있어도_채널을_지우고_남은_문장을_버린다() {
        states.withLockedState(GUILD, state -> state.join(GENERAL, channel -> false));

        service.clear(new ClearBotVoiceStateCommand(GUILD));

        assertThat(states.channelOf(GUILD)).isEmpty();
        assertThat(player.actions()).containsExactly("forget:1");
    }

    @Test
    void 나갔다가_다시_초대되면_사람이_남아_있던_채널과_상관없이_다른_채널에서_읽을_수_있다() {
        states.withLockedState(GUILD, state -> state.join(GENERAL, channel -> false));

        service.clear(new ClearBotVoiceStateCommand(GUILD));

        // 나가기 전 채널에 사람이 남아 있어도 막지 않는다.
        states.withLockedState(GUILD, state -> assertThat(state.canJoin(GAME, GENERAL::equals)).isTrue());
    }
}
