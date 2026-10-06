package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class QuickChatServiceTest {

    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final VoiceChannel CHANNEL = new VoiceChannel(1L, 2L);
    private static final VoiceChannelDetails DETAILS = new VoiceChannelDetails(CHANNEL, "우리 서버", null, "일반");

    private final List<String> played = new ArrayList<>();
    private final SpeechPlayer recordingPlayer =
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
    void 보내기_전에_어느_음성_채널에서_읽힐지_알려_준다() {
        QuickChatService service = service(user -> user.equals(SPEAKER) ? Optional.of(DETAILS) : Optional.empty());

        assertThat(service.findMyVoiceChannel(SPEAKER)).contains(DETAILS);
        assertThat(service.findMyVoiceChannel(new DiscordUserId(7L))).isEmpty();
    }

    private QuickChatService service(VoiceChannelLocator locator) {
        return new QuickChatService(locator, synthesizer, recordingPlayer, voices);
    }
}
