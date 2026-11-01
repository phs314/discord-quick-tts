package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceStatePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.stereotype.Component;

/**
 * 봇이 들어가 있는 디스코드 서버들의 음성 상태 캐시에서 사용자가 있는 음성 채널과 채널에 남은 사람을 본다.
 */
@Component
@RequiredArgsConstructor
public class VoiceStateDiscordAdapter implements VoiceStatePort {

    private final ShardManager shardManager;

    @Override
    public Optional<VoiceChannelDto> findUserVoiceChannel(DiscordUserId user) {
        return shardManager.getGuilds().stream()
                .map(guild -> guild.getMemberById(user.value()))
                .filter(Objects::nonNull)
                .map(VoiceStateDiscordAdapter::currentChannel)
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<VoiceChannelDto> findChannelDetails(VoiceChannel channel) {
        return findAudioChannel(channel).map(audioChannel -> toDetails(audioChannel.getGuild(), audioChannel));
    }

    @Override
    public boolean hasHumanMembers(VoiceChannel channel) {
        return findAudioChannel(channel)
                .map(audioChannel -> audioChannel.getMembers().stream().map(Member::getUser).anyMatch(user -> !user.isBot()))
                .orElse(false);
    }

    private Optional<AudioChannel> findAudioChannel(VoiceChannel channel) {
        Guild guild = shardManager.getGuildById(channel.guildId().value());
        return Optional.ofNullable(guild == null ? null : guild.getChannelById(AudioChannel.class, channel.channelId().value()));
    }

    private static Optional<VoiceChannelDto> currentChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel channel = voiceState == null ? null : voiceState.getChannel();
        return channel == null ? Optional.empty() : Optional.of(toDetails(member.getGuild(), channel));
    }

    private static VoiceChannelDto toDetails(Guild guild, AudioChannel channel) {
        return new VoiceChannelDto(
                new VoiceChannel(new GuildId(guild.getIdLong()), new ChannelId(channel.getIdLong())),
                guild.getName(),
                guild.getIconUrl(),
                channel.getName());
    }
}
