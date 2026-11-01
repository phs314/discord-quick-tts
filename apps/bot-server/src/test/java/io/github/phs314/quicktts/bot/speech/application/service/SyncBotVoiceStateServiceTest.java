package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SyncBotVoiceStateCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeBotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SyncBotVoiceStateServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final VoiceChannel GENERAL = new VoiceChannel(GUILD, 2L);
    private static final VoiceChannel GAME = new VoiceChannel(GUILD, 3L);

    private final FakeSpeechPlayerPort player = new FakeSpeechPlayerPort();
    private final FakeBotVoiceStatePort states = new FakeBotVoiceStatePort();
    private final SyncBotVoiceStateService service = new SyncBotVoiceStateService(states, player);

    @Test
    void 관리자가_봇을_다른_채널로_옮기면_상태도_따라간다() {
        join(GENERAL);

        service.sync(new SyncBotVoiceStateCommand(GENERAL, Optional.of(GAME)));

        assertThat(states.channelOf(GUILD)).contains(GAME);
        assertThat(player.actions()).isEmpty();
    }

    @Test
    void 관리자가_봇의_연결을_끊으면_채널을_지우고_남은_문장을_버린다() {
        join(GENERAL);

        service.sync(new SyncBotVoiceStateCommand(GENERAL, Optional.empty()));

        assertThat(states.channelOf(GUILD)).isEmpty();
        assertThat(player.actions()).containsExactly("leave:1");
    }

    @Test
    void 봇_서버가_이미_다른_채널로_옮겼으면_늦게_온_소식은_무시한다() {
        join(GAME);

        service.sync(new SyncBotVoiceStateCommand(GENERAL, Optional.empty()));

        assertThat(states.channelOf(GUILD)).contains(GAME);
        assertThat(player.actions()).isEmpty();
    }

    private void join(VoiceChannel channel) {
        states.withLockedState(GUILD, state -> state.join(channel, other -> false));
    }
}
