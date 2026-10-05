package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.domain.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.Speech;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class QuickChatServiceTest {

    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final VoiceChannel CHANNEL = new VoiceChannel(1L, 2L);

    private final List<String> played = new ArrayList<>();

    @Test
    void 보낸_사람이_있는_음성_채널에서_문장을_읽어_준다() {
        QuickChatService service = new QuickChatService(
                user -> user.equals(SPEAKER) ? Optional.of(CHANNEL) : Optional.empty(),
                message -> new Speech(message.text().getBytes(StandardCharsets.UTF_8), "txt"),
                (channel, speech) -> played.add(channel + ":" + new String(speech.audio(), StandardCharsets.UTF_8)));

        service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕")));

        assertThat(played).containsExactly(CHANNEL + ":안녕");
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_음성을_만들지_않는다() {
        QuickChatService service = new QuickChatService(
                user -> Optional.empty(),
                message -> {
                    throw new AssertionError("음성을 만들면 안 됩니다.");
                },
                (channel, speech) -> played.add("played"));

        assertThatThrownBy(() -> service.speak(new SpeakQuickChatCommand(SPEAKER, new QuickChatMessage("안녕"))))
                .isInstanceOf(SpeakerNotInVoiceChannelException.class);
        assertThat(played).isEmpty();
    }
}
