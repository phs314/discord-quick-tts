package io.github.phs314.quicktts.bot.speech.adapter.in.discord;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.SyncBotVoiceStateUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.LeaveEmptyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SyncBotVoiceStateCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.LeaveEmptyVoiceChannelCommand;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.junit.jupiter.api.Test;

class VoiceStateListenerTest {

    private static final GuildId GUILD_ID = new GuildId(100L);
    private static final long BOT = 1L;
    private static final long PERSON = 2L;

    private final LeaveEmptyVoiceChannelUseCase leaveEmptyVoiceChannelUseCase = mock(LeaveEmptyVoiceChannelUseCase.class);
    private final SyncBotVoiceStateUseCase syncBotVoiceStateUseCase = mock(SyncBotVoiceStateUseCase.class);
    private final VoiceStateListener listener = new VoiceStateListener(leaveEmptyVoiceChannelUseCase, syncBotVoiceStateUseCase);

    @Test
    void 들어오기만_한_소식은_넘긴다() {
        listener.onGuildVoiceUpdate(voiceUpdate(PERSON, null, channel(10L)));

        verifyNoInteractions(leaveEmptyVoiceChannelUseCase, syncBotVoiceStateUseCase);
    }

    @Test
    void 사람이_나가거나_옮기면_봇이_있는_채널이_비었는지_본다() {
        listener.onGuildVoiceUpdate(voiceUpdate(PERSON, channel(10L), channel(20L)));

        verify(leaveEmptyVoiceChannelUseCase).leaveIfEmpty(new LeaveEmptyVoiceChannelCommand(GUILD_ID));
        verifyNoInteractions(syncBotVoiceStateUseCase);
    }

    @Test
    void 봇이_다른_채널로_옮겨지면_상태도_따라_옮긴다() {
        listener.onGuildVoiceUpdate(voiceUpdate(BOT, channel(10L), channel(20L)));

        verify(syncBotVoiceStateUseCase).sync(new SyncBotVoiceStateCommand(
                new VoiceChannel(GUILD_ID, 10L), Optional.of(new VoiceChannel(GUILD_ID, 20L))));
        verifyNoInteractions(leaveEmptyVoiceChannelUseCase);
    }

    @Test
    void 봇이_채널에서_끊기면_채널을_지운다() {
        listener.onGuildVoiceUpdate(voiceUpdate(BOT, channel(10L), null));

        verify(syncBotVoiceStateUseCase).sync(new SyncBotVoiceStateCommand(
                new VoiceChannel(GUILD_ID, 10L), Optional.empty()));
    }

    private static GuildVoiceUpdateEvent voiceUpdate(long memberId, AudioChannelUnion left, AudioChannelUnion joined) {
        GuildVoiceUpdateEvent event = mock(GuildVoiceUpdateEvent.class, RETURNS_DEEP_STUBS);
        when(event.getChannelLeft()).thenReturn(left);
        when(event.getChannelJoined()).thenReturn(joined);
        when(event.getGuild().getIdLong()).thenReturn(GUILD_ID.value());
        when(event.getMember().getIdLong()).thenReturn(memberId);
        when(event.getJDA().getSelfUser().getIdLong()).thenReturn(BOT);
        return event;
    }

    private static AudioChannelUnion channel(long channelId) {
        AudioChannelUnion channel = mock(AudioChannelUnion.class);
        when(channel.getIdLong()).thenReturn(channelId);
        return channel;
    }
}
