package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import org.junit.jupiter.api.Test;

class LeaveEmptyVoiceChannelServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final DiscordUserId SPEAKER = new DiscordUserId(42L);
    private static final VoiceChannelDetails GENERAL =
            new VoiceChannelDetails(new VoiceChannel(GUILD, 2L), "우리 서버", null, "일반");

    private final FakeVoiceChannelLocator discord = new FakeVoiceChannelLocator();
    private final RecordingSpeechPlayer player = new RecordingSpeechPlayer();
    private final FakeBotSeatPort seats = new FakeBotSeatPort();
    private final LeaveEmptyVoiceChannelService service = new LeaveEmptyVoiceChannelService(seats, discord, player);

    @Test
    void 봇이_있는_채널에_사람이_아무도_없으면_나간다() {
        sitIn(GENERAL);
        discord.empty(GENERAL.channel());

        service.leaveIfEmpty(GUILD);

        assertThat(player.actions).containsExactly("leave");
        assertThat(seats.channelOf(GUILD)).isEmpty();
    }

    @Test
    void 봇이_있는_채널에_사람이_남아_있으면_그대로_있는다() {
        sitIn(GENERAL);

        service.leaveIfEmpty(GUILD);

        assertThat(player.actions).isEmpty();
        assertThat(seats.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 봇이_어느_채널에도_없으면_아무것도_하지_않는다() {
        service.leaveIfEmpty(GUILD);

        assertThat(player.actions).isEmpty();
    }

    private void sitIn(VoiceChannelDetails details) {
        discord.enter(SPEAKER, details);
        seats.withSeat(GUILD, seat -> seat.sitIn(details.channel(), discord::hasPeople));
    }
}
