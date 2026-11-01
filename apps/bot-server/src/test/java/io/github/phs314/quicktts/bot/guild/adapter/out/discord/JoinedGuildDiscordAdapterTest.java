package io.github.phs314.quicktts.bot.guild.adapter.out.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JoinedGuildDiscordAdapterTest {

    private static final GuildId GUILD_ID = new GuildId(100L);

    private final JDA jda = mock(JDA.class);
    private final Guild guild = mock(Guild.class, RETURNS_DEEP_STUBS);
    private final JoinedGuildDiscordAdapter joinedGuild = new JoinedGuildDiscordAdapter(jda);

    @BeforeEach
    void setUp() {
        when(jda.getGuildById(GUILD_ID.value())).thenReturn(guild);
        when(guild.getId()).thenReturn("100");
    }

    @Test
    void 시스템_채널에_안내를_남긴_뒤에_나간다() {
        TextChannel system = channel(true);
        when(guild.getSystemChannel()).thenReturn(system);
        MessageCreateAction send = sendFrom(system);

        joinedGuild.leaveWithNotice(GUILD_ID);

        ArgumentCaptor<String> notice = ArgumentCaptor.forClass(String.class);
        verify(system).sendMessage(notice.capture());
        assertThat(notice.getValue()).contains("`100`");
        verify(guild, never()).leave();

        onSuccess(send).accept(mock(Message.class));
        verify(guild.leave()).queue();
    }

    @Test
    void 안내를_남기지_못해도_나간다() {
        TextChannel system = channel(true);
        when(guild.getSystemChannel()).thenReturn(system);
        MessageCreateAction send = sendFrom(system);

        joinedGuild.leaveWithNotice(GUILD_ID);

        onFailure(send).accept(new RuntimeException("권한 없음"));
        verify(guild.leave()).queue();
    }

    @Test
    void 시스템_채널에_쓸_수_없으면_기본_채널에_남긴다() {
        TextChannel system = channel(false);
        when(guild.getSystemChannel()).thenReturn(system);
        DefaultGuildChannelUnion fallback = mock(DefaultGuildChannelUnion.class,
                withSettings().extraInterfaces(GuildMessageChannel.class).defaultAnswer(RETURNS_DEEP_STUBS));
        when(((GuildMessageChannel) fallback).canTalk()).thenReturn(true);
        when(guild.getDefaultChannel()).thenReturn(fallback);

        joinedGuild.leaveWithNotice(GUILD_ID);

        verify(system, never()).sendMessage(anyString());
        verify((GuildMessageChannel) fallback).sendMessage(anyString());
    }

    @Test
    void 글을_쓸_채널이_없으면_안내_없이_나간다() {
        when(guild.getSystemChannel()).thenReturn(null);
        when(guild.getDefaultChannel()).thenReturn(null);

        joinedGuild.leaveWithNotice(GUILD_ID);

        verify(guild.leave()).queue();
    }

    @Test
    void 이미_나간_서버면_아무것도_하지_않는다() {
        when(jda.getGuildById(GUILD_ID.value())).thenReturn(null);

        joinedGuild.leaveWithNotice(GUILD_ID);

        verify(guild, never()).leave();
    }

    private static TextChannel channel(boolean canTalk) {
        TextChannel channel = mock(TextChannel.class, RETURNS_DEEP_STUBS);
        when(channel.canTalk()).thenReturn(canTalk);
        return channel;
    }

    private static MessageCreateAction sendFrom(GuildMessageChannel channel) {
        MessageCreateAction send = mock(MessageCreateAction.class);
        when(channel.sendMessage(anyString())).thenReturn(send);
        return send;
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Message> onSuccess(MessageCreateAction send) {
        ArgumentCaptor<Consumer<Message>> success = ArgumentCaptor.forClass(Consumer.class);
        verify(send).queue(success.capture(), any());
        return success.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Throwable> onFailure(MessageCreateAction send) {
        ArgumentCaptor<Consumer<Throwable>> failure = ArgumentCaptor.forClass(Consumer.class);
        verify(send).queue(any(), failure.capture());
        return failure.getValue();
    }
}
