package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.jdbc.core.simple.JdbcClient;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class JdbcAllowedGuildRepositoryTest {

    @Parameter
    TestDatabase database;

    private static final GuildId FIRST = new GuildId(987654321098765432L);
    private static final GuildId SECOND = new GuildId(123456789012345678L);
    private static final Instant EARLIER = Instant.parse("2026-10-11T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-10-11T01:00:00Z");

    private JdbcAllowedGuildRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcAllowedGuildRepository(JdbcClient.create(database.dataSource()));
    }

    @Test
    void 넣은_순서대로_돌려준다() {
        repository.add(AllowedGuild.allow(SECOND, LATER));
        repository.add(AllowedGuild.allow(FIRST, EARLIER));

        assertThat(repository.findAll()).extracting(AllowedGuild::guildId, AllowedGuild::allowedAt)
                .containsExactly(
                        tuple(FIRST, EARLIER),
                        tuple(SECOND, LATER));
    }

    @Test
    void 이미_있는_서버는_다시_넣지_않는다() {
        assertThat(repository.add(AllowedGuild.allow(FIRST, EARLIER))).isTrue();
        assertThat(repository.add(AllowedGuild.allow(FIRST, LATER))).isFalse();

        assertThat(repository.findAll()).extracting(AllowedGuild::allowedAt).containsExactly(EARLIER);
    }

    @Test
    void 지우면_없다() {
        repository.add(AllowedGuild.allow(FIRST, EARLIER));

        assertThat(repository.delete(FIRST)).isTrue();
        assertThat(repository.delete(FIRST)).isFalse();
        assertThat(repository.findAll()).isEmpty();
    }
}
