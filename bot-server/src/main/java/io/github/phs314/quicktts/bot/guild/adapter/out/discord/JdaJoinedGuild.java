package io.github.phs314.quicktts.bot.guild.adapter.out.discord;

import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JDA 로 봇이 들어가 있는 디스코드 서버를 보고, 안내 메시지를 남긴 뒤 나간다.
 */
@Component
@RequiredArgsConstructor
public class JdaJoinedGuild implements JoinedGuildPort {

    private static final Logger log = LoggerFactory.getLogger(JdaJoinedGuild.class);

    private final JDA jda;

    @Override
    public List<GuildId> joinedGuilds() {
        return jda.getGuilds().stream().map(guild -> new GuildId(guild.getIdLong())).toList();
    }

    @Override
    public void leaveWithNotice(GuildId guildId) {
        Guild guild = jda.getGuildById(guildId.value());
        if (guild == null) {
            return;
        }
        log.info("허용 목록에 없는 디스코드 서버에서 나갑니다: {} ({})", guild.getName(), guild.getId());
        GuildMessageChannel channel = noticeChannel(guild);
        if (channel == null) {
            guild.leave().queue();
            return;
        }
        // 안내가 실패해도 나가는 건 해야 하므로 성공, 실패 모두 나간다.
        channel.sendMessage(notice(guild))
                .queue(sent -> guild.leave().queue(), error -> guild.leave().queue());
    }

    /** 시스템 메시지 채널, 없으면 기본 채널. 봇이 글을 쓸 수 없으면 null. */
    private static GuildMessageChannel noticeChannel(Guild guild) {
        GuildMessageChannel system = guild.getSystemChannel();
        if (system != null && system.canTalk()) {
            return system;
        }
        GuildMessageChannel fallback = guild.getDefaultChannel() instanceof GuildMessageChannel channel ? channel : null;
        return fallback != null && fallback.canTalk() ? fallback : null;
    }

    private static String notice(Guild guild) {
        return "Quick TTS 봇은 아직 허용된 디스코드 서버에서만 쓸 수 있어서 이 서버에서 나갑니다.\n"
                + "쓰고 싶으면 봇 주인에게 이 서버 ID 를 알려 주세요: `" + guild.getId() + "`";
    }
}
