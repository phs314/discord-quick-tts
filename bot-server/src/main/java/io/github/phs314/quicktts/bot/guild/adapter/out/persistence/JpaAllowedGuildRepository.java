package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 허용 목록을 {@code allowed_guild} 테이블에 저장한다. 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class JpaAllowedGuildRepository implements AllowedGuildPort {

    private final SpringDataAllowedGuildRepository springDataAllowedGuildRepository;

    @Override
    public List<AllowedGuild> findAll() {
        return springDataAllowedGuildRepository.findAllByOrderByAllowedAtAscGuildIdAsc().stream()
                .map(AllowedGuildPersistenceMapper::toDomain)
                .toList();
    }

    /** 이미 있으면 넣지 않는다. 있는지 본 뒤 다른 요청이 같은 서버를 먼저 넣으면 기본 키 때문에 커밋에서 실패한다. */
    @Override
    public boolean saveIfAbsent(AllowedGuild allowedGuild) {
        if (springDataAllowedGuildRepository.existsById(allowedGuild.guildId().value())) {
            return false;
        }
        springDataAllowedGuildRepository.save(AllowedGuildPersistenceMapper.toEntity(allowedGuild));
        return true;
    }

    /** 모두 함께 넣거나 하나도 넣지 않는 것은 부르는 유스케이스의 트랜잭션이 맡는다 (ADR 0033). */
    @Override
    public void saveAll(List<AllowedGuild> allowedGuilds) {
        springDataAllowedGuildRepository.saveAll(allowedGuilds.stream()
                .map(AllowedGuildPersistenceMapper::toEntity)
                .toList());
    }

    @Override
    public boolean delete(GuildId guildId) {
        return springDataAllowedGuildRepository.deleteByGuildId(guildId.value()) > 0;
    }
}
