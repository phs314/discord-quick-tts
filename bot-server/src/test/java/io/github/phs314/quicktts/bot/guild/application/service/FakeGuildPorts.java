package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** guild 서비스 테스트에서 쓰는 메모리 출력 포트들. */
final class FakeGuildPorts {

    private FakeGuildPorts() {
    }

    static class InMemoryAllowedGuilds implements AllowedGuildPort {

        final Map<GuildId, AllowedGuild> byGuild = new LinkedHashMap<>();

        @Override
        public List<AllowedGuild> findAll() {
            return List.copyOf(byGuild.values());
        }

        @Override
        public boolean add(AllowedGuild allowedGuild) {
            return byGuild.putIfAbsent(allowedGuild.guildId(), allowedGuild) == null;
        }

        @Override
        public boolean delete(GuildId guild) {
            return byGuild.remove(guild) != null;
        }
    }

    /** 봇이 들어가 있는 디스코드 서버. 나가면 목록에서 빠지고 {@link #left} 에 남는다. */
    static class RecordingJoinedGuilds implements JoinedGuildPort {

        final List<GuildId> joined = new ArrayList<>();
        final List<GuildId> left = new ArrayList<>();

        RecordingJoinedGuilds(GuildId... joined) {
            this.joined.addAll(List.of(joined));
        }

        @Override
        public List<GuildId> joinedGuilds() {
            return List.copyOf(joined);
        }

        @Override
        public void leaveNotAllowed(GuildId guild) {
            if (joined.remove(guild)) {
                left.add(guild);
            }
        }
    }

    static class FixedSetting implements GuildAllowlistSettingPort {

        boolean enabled = true;

        @Override
        public boolean enabled() {
            return enabled;
        }
    }
}
