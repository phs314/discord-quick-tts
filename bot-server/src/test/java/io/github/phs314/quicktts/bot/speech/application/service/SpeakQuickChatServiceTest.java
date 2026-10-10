package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import org.junit.jupiter.api.Test;

class SpeakQuickChatServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final DiscordUserId FRIEND = new DiscordUserId(43L);
    private static final VoiceChannelDetails GENERAL =
            new VoiceChannelDetails(new VoiceChannel(GUILD, 2L), "우리 서버", null, "일반");
    private static final VoiceChannelDetails GAME =
            new VoiceChannelDetails(new VoiceChannel(GUILD, 3L), "우리 서버", null, "게임");
    private static final VoiceChannelDetails OTHER_SERVER =
            new VoiceChannelDetails(new VoiceChannel(new GuildId(9L), 4L), "다른 서버", null, "일반");

    private final FakeVoiceChannelLocator discord = new FakeVoiceChannelLocator();
    private final RecordingSpeechPlayer player = new RecordingSpeechPlayer();
    private final FakeBotSeatPort seats = new FakeBotSeatPort();
    private final ManageVoiceServiceTest.FakeSynthesizer synthesizer = new ManageVoiceServiceTest.FakeSynthesizer();
    private final ManageVoiceService voices =
            new ManageVoiceService(synthesizer, new ManageVoiceServiceTest.InMemoryVoicePreferences());
    private final SpeakQuickChatService service = new SpeakQuickChatService(discord, synthesizer, player, seats, voices);

    @Test
    void 보낸_사람이_있는_음성_채널에_들어가서_그_사람이_고른_목소리로_읽어_준다() {
        voices.changeVoice(SPEAKER, ManageVoiceServiceTest.INJOON.id());
        discord.enter(SPEAKER, GENERAL);

        speak(SPEAKER, "안녕");

        assertThat(player.actions).containsExactly("join:2", "play:" + ManageVoiceServiceTest.INJOON.id().value() + ":안녕");
        assertThat(seats.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_읽지_않는다() {
        assertThatThrownBy(() -> speak(SPEAKER, "안녕")).isInstanceOf(SpeakerNotInVoiceChannelException.class);
        assertThat(player.actions).isEmpty();
    }

    @Test
    void 봇이_같은_서버의_다른_채널에서_쓰이고_있으면_옮겨_가지_않는다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.enter(SPEAKER, GENERAL);

        assertThatThrownBy(() -> speak(SPEAKER, "안녕"))
                .isInstanceOf(VoiceChannelInUseException.class)
                .hasMessageContaining("게임");
        assertThat(player.actions).containsExactly("join:3", "play:edge:sunhi:먼저");
        assertThat(seats.channelOf(GUILD)).contains(GAME.channel());
    }

    @Test
    void 봇이_들어가는_중이어도_같은_서버의_다른_채널은_막는다() {
        // 디스코드 음성 연결이 끝났는지와 상관없이, 봇 서버가 들어가기로 정한 순간 자리가 잡힌다.
        discord.enter(FRIEND, GAME);
        discord.enter(SPEAKER, GENERAL);
        speak(FRIEND, "먼저");

        assertThatThrownBy(() -> speak(SPEAKER, "안녕")).isInstanceOf(VoiceChannelInUseException.class);
    }

    @Test
    void 봇이_보낸_사람과_같은_채널에_있으면_다시_들어가지_않고_이어서_읽는다() {
        discord.enter(SPEAKER, GENERAL);
        discord.enter(FRIEND, GENERAL);
        speak(FRIEND, "먼저");

        speak(SPEAKER, "안녕");

        assertThat(player.actions).containsExactly("join:2", "play:edge:sunhi:먼저", "join:2", "play:edge:sunhi:안녕");
    }

    @Test
    void 봇이_있던_채널이_비었으면_보낸_사람의_채널로_옮겨_간다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.empty(GAME.channel());
        discord.enter(SPEAKER, GENERAL);

        speak(SPEAKER, "안녕");

        assertThat(player.actions).endsWith("join:2", "play:edge:sunhi:안녕");
        assertThat(seats.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 다른_디스코드_서버의_자리는_상관없다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.enter(SPEAKER, OTHER_SERVER);

        speak(SPEAKER, "안녕");

        assertThat(seats.channelOf(OTHER_SERVER.channel().guildId())).contains(OTHER_SERVER.channel());
        assertThat(seats.channelOf(GUILD)).contains(GAME.channel());
    }

    @Test
    void 채널에_들어가지_못하면_자리를_잡지_않는다() {
        discord.enter(SPEAKER, GENERAL);
        SpeakQuickChatService failingToJoin = new SpeakQuickChatService(discord, synthesizer, new RecordingSpeechPlayer() {
            @Override
            public void join(VoiceChannel channel) {
                throw new IllegalStateException("음성 채널에 들어갈 권한이 없습니다.");
            }
        }, seats, voices);

        assertThatThrownBy(() -> failingToJoin.speak(command(SPEAKER, "안녕"))).isInstanceOf(IllegalStateException.class);
        assertThat(seats.channelOf(GUILD)).isEmpty();
    }

    private void speak(DiscordUserId speaker, String message) {
        service.speak(command(speaker, message));
    }

    private static SpeakQuickChatCommand command(DiscordUserId speaker, String message) {
        return new SpeakQuickChatCommand(speaker, new QuickChatMessage(message));
    }
}
