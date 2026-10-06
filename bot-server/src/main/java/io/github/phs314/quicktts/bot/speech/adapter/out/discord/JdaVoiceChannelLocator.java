package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Component;

/**
 * 봇이 들어가 있는 서버들의 음성 상태 캐시에서 사용자를 찾는다.
 */
@Component
public class JdaVoiceChannelLocator implements VoiceChannelLocator {

    private final JDA jda;

    public JdaVoiceChannelLocator(JDA jda) {
        this.jda = jda;
    }

    @Override
    public Optional<VoiceChannelDetails> findCurrentChannel(DiscordUserId user) {
        return jda.getGuilds().stream()
                .map(guild -> guild.getMemberById(user.value()))
                .filter(Objects::nonNull)
                .map(JdaVoiceChannelLocator::toDetails)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<VoiceChannelDetails> toDetails(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel channel = voiceState == null ? null : voiceState.getChannel();
        if (channel == null) {
            return Optional.empty();
        }
        Guild guild = member.getGuild();
        return Optional.of(new VoiceChannelDetails(
                new VoiceChannel(guild.getIdLong(), channel.getIdLong()),
                guild.getName(),
                guild.getIconUrl(),
                channel.getName()));
    }
}
