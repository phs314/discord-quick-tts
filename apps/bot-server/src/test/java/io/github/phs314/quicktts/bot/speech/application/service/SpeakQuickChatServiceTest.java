package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.speech.application.exception.AuthorNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeBotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeTextToSpeechPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeUserVoicePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import org.junit.jupiter.api.Test;

class SpeakQuickChatServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final DiscordUserId AUTHOR = new DiscordUserId(42L);
    private static final DiscordUserId FRIEND = new DiscordUserId(43L);
    private static final VoiceChannelDto GENERAL =
            new VoiceChannelDto(new VoiceChannel(GUILD, new ChannelId(2L)), "우리 서버", null, "일반");
    private static final VoiceChannelDto GAME =
            new VoiceChannelDto(new VoiceChannel(GUILD, new ChannelId(3L)), "우리 서버", null, "게임");
    private static final VoiceChannelDto OTHER_SERVER =
            new VoiceChannelDto(new VoiceChannel(new GuildId(9L), new ChannelId(4L)), "다른 서버", null, "일반");

    private final FakeVoiceStatePort discord = new FakeVoiceStatePort();
    private final FakeSpeechPlayerPort player = new FakeSpeechPlayerPort();
    private final FakeBotVoiceStatePort states = new FakeBotVoiceStatePort();
    private final FakeUserVoicePort voices = new FakeUserVoicePort();
    private final FakeTextToSpeechPort tts = new FakeTextToSpeechPort();
    private final SpeakQuickChatService service =
            new SpeakQuickChatService(discord, tts, player, states, voices);

    @Test
    void 보낸_사람이_있는_음성_채널에_들어가서_그_사람이_고른_목소리로_읽어_준다() {
        voices.choose(AUTHOR, new VoiceId("fake:injoon"));
        discord.enter(AUTHOR, GENERAL);

        speak(AUTHOR, "안녕");

        assertThat(player.actions()).containsExactly("join:2", "play:fake:injoon:안녕");
        assertThat(states.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 다듬은_문장을_읽는다() {
        discord.enter(AUTHOR, GENERAL);

        speak(AUTHOR, "ㅋㅋ 좋아👍");

        assertThat(player.actions()).containsExactly("join:2", "play:fake:sunhi:키키 좋아");
    }

    @Test
    void 다듬고_나서_읽을_내용이_없으면_채널에_들어가지_않는다() {
        discord.enter(AUTHOR, GENERAL);

        assertThatThrownBy(() -> speak(AUTHOR, "👍")).isInstanceOf(InvalidDomainValueException.class);
        assertThat(player.actions()).isEmpty();
        assertThat(states.channelOf(GUILD)).isEmpty();
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_읽지_않는다() {
        assertThatThrownBy(() -> speak(AUTHOR, "안녕")).isInstanceOf(AuthorNotInVoiceChannelException.class);
        assertThat(player.actions()).isEmpty();
    }

    @Test
    void 봇이_같은_서버의_다른_채널에서_쓰이고_있으면_옮겨_가지_않는다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.enter(AUTHOR, GENERAL);

        assertThatThrownBy(() -> speak(AUTHOR, "안녕"))
                .isInstanceOf(VoiceChannelInUseException.class)
                .hasMessageContaining("게임");
        assertThat(player.actions()).containsExactly("join:3", "play:fake:sunhi:먼저");
        assertThat(states.channelOf(GUILD)).contains(GAME.channel());
    }

    @Test
    void 봇이_들어가는_중이어도_같은_서버의_다른_채널은_막는다() {
        // 디스코드 음성 연결이 끝났는지와 상관없이, 봇 서버가 들어가기로 정한 순간 채널이 정해진다.
        discord.enter(FRIEND, GAME);
        discord.enter(AUTHOR, GENERAL);
        speak(FRIEND, "먼저");

        assertThatThrownBy(() -> speak(AUTHOR, "안녕")).isInstanceOf(VoiceChannelInUseException.class);
    }

    @Test
    void 봇이_보낸_사람과_같은_채널에_있으면_다시_들어가지_않고_이어서_읽는다() {
        discord.enter(AUTHOR, GENERAL);
        discord.enter(FRIEND, GENERAL);
        speak(FRIEND, "먼저");

        speak(AUTHOR, "안녕");

        assertThat(player.actions()).containsExactly("join:2", "play:fake:sunhi:먼저", "join:2", "play:fake:sunhi:안녕");
    }

    @Test
    void 봇이_있던_채널이_비었으면_보낸_사람의_채널로_옮겨_간다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.empty(GAME.channel());
        discord.enter(AUTHOR, GENERAL);

        speak(AUTHOR, "안녕");

        assertThat(player.actions()).endsWith("join:2", "play:fake:sunhi:안녕");
        assertThat(states.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 다른_디스코드_서버의_음성_상태는_상관없다() {
        discord.enter(FRIEND, GAME);
        speak(FRIEND, "먼저");
        discord.enter(AUTHOR, OTHER_SERVER);

        speak(AUTHOR, "안녕");

        assertThat(states.channelOf(OTHER_SERVER.channel().guildId())).contains(OTHER_SERVER.channel());
        assertThat(states.channelOf(GUILD)).contains(GAME.channel());
    }

    @Test
    void 채널에_들어가지_못하면_채널을_적지_않는다() {
        discord.enter(AUTHOR, GENERAL);
        SpeakQuickChatService failingToJoin = new SpeakQuickChatService(discord, tts, new FakeSpeechPlayerPort() {
            @Override
            public void join(VoiceChannel channel) {
                throw new BotCannotJoinVoiceChannelException(channel, null);
            }
        }, states, voices);

        assertThatThrownBy(() -> failingToJoin.speak(command(AUTHOR, "안녕")))
                .isInstanceOf(BotCannotJoinVoiceChannelException.class);
        assertThat(states.channelOf(GUILD)).isEmpty();
    }

    private void speak(DiscordUserId author, String message) {
        service.speak(command(author, message));
    }

    private static SpeakQuickChatCommand command(DiscordUserId author, String message) {
        return new SpeakQuickChatCommand(author, new QuickChatMessage(message));
    }
}
