package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import org.junit.jupiter.api.Test;

class FindCurrentUserVoiceChannelServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final VoiceChannelDto DETAILS =
            new VoiceChannelDto(new VoiceChannel(new GuildId(1L), new ChannelId(2L)), "우리 서버", null, "일반");

    @Test
    void 보내기_전에_어느_음성_채널에서_읽힐지_알려_준다() {
        FakeVoiceStatePort discord = new FakeVoiceStatePort();
        discord.enter(USER, DETAILS);
        FindCurrentUserVoiceChannelService service = new FindCurrentUserVoiceChannelService(discord);

        assertThat(service.findCurrentUserVoiceChannel(new FindCurrentUserVoiceChannelQuery(USER))).contains(DETAILS);
        assertThat(service.findCurrentUserVoiceChannel(new FindCurrentUserVoiceChannelQuery(new DiscordUserId(7L)))).isEmpty();
    }
}
