package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.LeaveEmptyVoiceChannelCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeBotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import org.junit.jupiter.api.Test;

class LeaveEmptyVoiceChannelServiceTest {

    private static final GuildId GUILD = new GuildId(1L);
    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final VoiceChannelDto GENERAL =
            new VoiceChannelDto(new VoiceChannel(GUILD, new ChannelId(2L)), "우리 서버", null, "일반");

    private final FakeVoiceStatePort discord = new FakeVoiceStatePort();
    private final FakeSpeechPlayerPort player = new FakeSpeechPlayerPort();
    private final FakeBotVoiceStatePort states = new FakeBotVoiceStatePort();
    private final LeaveEmptyVoiceChannelService service = new LeaveEmptyVoiceChannelService(states, discord, player);

    @Test
    void 봇이_있는_채널에_사람이_아무도_없으면_나간다() {
        join(GENERAL);
        discord.empty(GENERAL.channel());

        service.leaveIfEmpty(new LeaveEmptyVoiceChannelCommand(GUILD));

        assertThat(player.actions()).containsExactly("leave:1");
        assertThat(states.channelOf(GUILD)).isEmpty();
    }

    @Test
    void 봇이_있는_채널에_사람이_남아_있으면_그대로_있는다() {
        join(GENERAL);

        service.leaveIfEmpty(new LeaveEmptyVoiceChannelCommand(GUILD));

        assertThat(player.actions()).isEmpty();
        assertThat(states.channelOf(GUILD)).contains(GENERAL.channel());
    }

    @Test
    void 봇이_어느_채널에도_없으면_아무것도_하지_않는다() {
        service.leaveIfEmpty(new LeaveEmptyVoiceChannelCommand(GUILD));

        assertThat(player.actions()).isEmpty();
    }

    private void join(VoiceChannelDto details) {
        discord.enter(USER, details);
        states.withLockedState(GUILD, state -> state.join(details.channel(), discord::hasHumanMembers));
    }
}
