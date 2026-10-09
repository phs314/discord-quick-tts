package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Component;

/**
 * 봇이 들어가 있는 서버들의 음성 상태 캐시에서 사용자와 봇이 있는 음성 채널을 찾는다.
 */
@Component
public class JdaVoiceChannelLocator implements VoiceChannelLocatorPort {

    private final JDA jda;

    public JdaVoiceChannelLocator(JDA jda) {
        this.jda = jda;
    }

    @Override
    public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
        return jda.getGuilds().stream()
                .map(guild -> guild.getMemberById(user.value()))
                .filter(Objects::nonNull)
                .map(JdaVoiceChannelLocator::currentChannel)
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<VoiceChannelDetails> findBotChannelInUse(GuildId guildId) {
        Guild guild = jda.getGuildById(guildId.value());
        AudioChannel channel = guild == null ? null : guild.getAudioManager().getConnectedChannel();
        if (channel == null || channel.getMembers().stream().map(Member::getUser).allMatch(User::isBot)) {
            return Optional.empty();
        }
        return Optional.of(toDetails(guild, channel));
    }

    private static Optional<VoiceChannelDetails> currentChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel channel = voiceState == null ? null : voiceState.getChannel();
        return channel == null ? Optional.empty() : Optional.of(toDetails(member.getGuild(), channel));
    }

    private static VoiceChannelDetails toDetails(Guild guild, AudioChannel channel) {
        return new VoiceChannelDetails(
                new VoiceChannel(new GuildId(guild.getIdLong()), channel.getIdLong()),
                guild.getName(),
                guild.getIconUrl(),
                channel.getName());
    }
}
