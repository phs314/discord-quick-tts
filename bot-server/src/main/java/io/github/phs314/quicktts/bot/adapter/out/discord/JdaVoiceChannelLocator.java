package io.github.phs314.quicktts.bot.adapter.out.discord;

import io.github.phs314.quicktts.bot.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.VoiceChannel;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
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
    public Optional<VoiceChannel> findCurrentChannel(DiscordUserId user) {
        return jda.getGuilds().stream()
                .map(guild -> guild.getMemberById(user.value()))
                .filter(Objects::nonNull)
                .map(JdaVoiceChannelLocator::toVoiceChannel)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<VoiceChannel> toVoiceChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            return Optional.empty();
        }
        return Optional.of(new VoiceChannel(member.getGuild().getIdLong(), voiceState.getChannel().getIdLong()));
    }
}
