package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * 봇이 있는 음성 채널에서 사람이 모두 나가면 봇도 바로 나간다.
 */
@Component
@RequiredArgsConstructor
public class EmptyVoiceChannelWatcher extends ListenerAdapter {

    private final JDA jda;
    private final LavaPlayerSpeechPlayer speechPlayer;

    @PostConstruct
    void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannel left = event.getChannelLeft();
        AudioChannel botChannel = event.getGuild().getAudioManager().getConnectedChannel();
        if (left == null || botChannel == null || left.getIdLong() != botChannel.getIdLong()) {
            return;
        }
        boolean onlyBotsLeft = botChannel.getMembers().stream().map(Member::getUser).allMatch(User::isBot);
        if (onlyBotsLeft) {
            speechPlayer.leave(new GuildId(event.getGuild().getIdLong()));
        }
    }
}
