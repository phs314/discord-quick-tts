package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class QuickChatServiceTest {

    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final VoiceChannel CHANNEL = new VoiceChannel(new GuildId(1L), 2L);
    private static final VoiceChannelDetails DETAILS = new VoiceChannelDetails(CHANNEL, "우리 서버", null, "일반");
    private static final VoiceChannelDetails OTHER_CHANNEL_IN_SAME_SERVER =
            new VoiceChannelDetails(new VoiceChannel(new GuildId(1L), 3L), "우리 서버", null, "게임");

    private final List<String> played = new ArrayList<>();
    private final SpeechPlayerPort recordingPlayer =
            (channel, speech) -> played.add(channel + ":" + new String(speech.audio(), StandardCharsets.UTF_8));
    private final VoiceServiceTest.FakeSynthesizer synthesizer = new VoiceServiceTest.FakeSynthesizer();
    private final VoiceService voices = new VoiceService(synthesizer, new VoiceServiceTest.InMemoryVoicePreferences());

    @Test
    void 보낸_사람이_있는_음성_채널에서_그_사람이_고른_목소리로_읽어_준다() {
        voices.changeVoice(SPEAKER, VoiceServiceTest.INJOON.id());
        QuickChatService service = service(user -> user.equals(SPEAKER) ? Optional.of(DETAILS) : Optional.empty());

        service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕")));

        assertThat(played).containsExactly(CHANNEL + ":" + VoiceServiceTest.INJOON.id().value() + ":안녕");
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_읽지_않는다() {
        QuickChatService service = service(user -> Optional.empty());

        assertThatThrownBy(() -> service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕"))))
                .isInstanceOf(SpeakerNotInVoiceChannelException.class);
        assertThat(played).isEmpty();
    }

    @Test
    void 봇이_같은_서버의_다른_채널에서_쓰이고_있으면_옮겨_가지_않는다() {
        QuickChatService service = service(speakerIn(DETAILS), guildId -> Optional.of(OTHER_CHANNEL_IN_SAME_SERVER));

        assertThatThrownBy(() -> service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕"))))
                .isInstanceOf(VoiceChannelInUseException.class)
                .hasMessageContaining("게임");
        assertThat(played).isEmpty();
    }

    @Test
    void 봇이_보낸_사람과_같은_채널에_있으면_읽어_준다() {
        QuickChatService service = service(speakerIn(DETAILS), guildId -> Optional.of(DETAILS));

        service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕")));

        assertThat(played).hasSize(1);
    }

    @Test
    void 봇이_있던_채널이_비었으면_보낸_사람의_채널로_옮겨_간다() {
        QuickChatService service = service(speakerIn(DETAILS), guildId -> Optional.empty());

        service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕")));

        assertThat(played).singleElement().asString().startsWith(CHANNEL + ":");
    }

    @Test
    void 보내기_전에_어느_음성_채널에서_읽힐지_알려_준다() {
        QuickChatService service = service(user -> user.equals(SPEAKER) ? Optional.of(DETAILS) : Optional.empty());

        assertThat(service.findMyVoiceChannel(SPEAKER)).contains(DETAILS);
        assertThat(service.findMyVoiceChannel(new DiscordUserId(7L))).isEmpty();
    }

    /** 봇은 어느 채널에서도 쓰이고 있지 않다. */
    private QuickChatService service(Function<DiscordUserId, Optional<VoiceChannelDetails>> speakers) {
        return service(speakers, guildId -> Optional.empty());
    }

    private QuickChatService service(Function<DiscordUserId, Optional<VoiceChannelDetails>> speakers,
                                     Function<GuildId, Optional<VoiceChannelDetails>> botChannelInUse) {
        return new QuickChatService(new FakeVoiceChannelLocator(speakers, botChannelInUse),
                synthesizer, recordingPlayer, voices);
    }

    private static Function<DiscordUserId, Optional<VoiceChannelDetails>> speakerIn(VoiceChannelDetails details) {
        return user -> user.equals(SPEAKER) ? Optional.of(details) : Optional.empty();
    }

    private record FakeVoiceChannelLocator(Function<DiscordUserId, Optional<VoiceChannelDetails>> speakers,
                                           Function<GuildId, Optional<VoiceChannelDetails>> botChannelInUse)
            implements VoiceChannelLocatorPort {

        @Override
        public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
            return speakers.apply(user);
        }

        @Override
        public Optional<VoiceChannelDetails> findBotChannelInUse(GuildId guildId) {
            return botChannelInUse.apply(guildId);
        }
    }
}
