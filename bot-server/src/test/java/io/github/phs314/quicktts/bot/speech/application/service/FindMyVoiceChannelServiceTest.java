package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FindMyVoiceChannelServiceTest {

    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final VoiceChannelDetails DETAILS =
            new VoiceChannelDetails(new VoiceChannel(new GuildId(1L), 2L), "우리 서버", null, "일반");

    @Test
    void 보내기_전에_어느_음성_채널에서_읽힐지_알려_준다() {
        FindMyVoiceChannelService service = new FindMyVoiceChannelService(new FakeVoiceChannelLocator(
                user -> user.equals(SPEAKER) ? Optional.of(DETAILS) : Optional.empty(),
                guildId -> Optional.empty()));

        assertThat(service.findMyVoiceChannel(SPEAKER)).contains(DETAILS);
        assertThat(service.findMyVoiceChannel(new DiscordUserId(7L))).isEmpty();
    }
}
