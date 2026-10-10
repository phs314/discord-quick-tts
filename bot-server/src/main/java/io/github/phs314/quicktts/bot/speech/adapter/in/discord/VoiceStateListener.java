package io.github.phs314.quicktts.bot.speech.adapter.in.discord;

import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.FollowBotMoveUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.LeaveEmptyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * 디스코드 음성 채널을 누가 나가거나 옮겼다는 소식을 봇 자리에 알린다.
 * 사람이 나갔으면 봇이 있는 채널이 비었는지 보고, 봇 자신이 옮겨지거나 끊겼으면 자리를 맞춘다.
 */
@Component
@RequiredArgsConstructor
public class VoiceStateListener extends ListenerAdapter {

    private final JDA jda;
    private final LeaveEmptyVoiceChannelUseCase leaveEmptyVoiceChannel;
    private final FollowBotMoveUseCase followBotMove;

    @PostConstruct
    void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannel left = event.getChannelLeft();
        if (left == null) {
            return;
        }
        GuildId guildId = new GuildId(event.getGuild().getIdLong());
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            followBotMove.followBotMove(toVoiceChannel(guildId, left),
                    Optional.ofNullable(event.getChannelJoined()).map(joined -> toVoiceChannel(guildId, joined)));
        } else {
            leaveEmptyVoiceChannel.leaveIfEmpty(guildId);
        }
    }

    private static VoiceChannel toVoiceChannel(GuildId guildId, AudioChannel channel) {
        return new VoiceChannel(guildId, channel.getIdLong());
    }
}
