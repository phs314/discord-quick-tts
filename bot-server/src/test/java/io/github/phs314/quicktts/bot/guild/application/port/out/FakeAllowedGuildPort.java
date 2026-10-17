package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 허용 목록 저장소 대신 쓰는 메모리 대역. */
public class FakeAllowedGuildPort implements AllowedGuildPort {

    private final Map<GuildId, AllowedGuild> byGuild = new LinkedHashMap<>();

    @Override
    public List<AllowedGuild> findAll() {
        return List.copyOf(byGuild.values());
    }

    @Override
    public boolean saveIfAbsent(AllowedGuild allowedGuild) {
        return byGuild.putIfAbsent(allowedGuild.guildId(), allowedGuild) == null;
    }

    @Override
    public void saveAll(List<AllowedGuild> allowedGuilds) {
        if (allowedGuilds.stream().anyMatch(allowedGuild -> byGuild.containsKey(allowedGuild.guildId()))) {
            throw new IllegalStateException("이미 있는 디스코드 서버");
        }
        allowedGuilds.forEach(allowedGuild -> byGuild.put(allowedGuild.guildId(), allowedGuild));
    }

    @Override
    public boolean delete(GuildId guildId) {
        return byGuild.remove(guildId) != null;
    }
}
