package io.github.phs314.quicktts.bot.guild.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import io.github.phs314.quicktts.bot.support.TestJpa;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class AllowedGuildPersistenceAdapterTest {

    @Parameter
    TestDatabase database;

    private static final GuildId FIRST = new GuildId(987654321098765432L);
    private static final GuildId SECOND = new GuildId(123456789012345678L);
    private static final Instant EARLIER = Instant.parse("2026-10-11T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-10-11T01:00:00Z");

    private AnnotationConfigApplicationContext context;
    private AllowedGuildPersistenceAdapter repository;
    /** 유스케이스의 {@code @Transactional} 대신 트랜잭션을 연다. */
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        context = TestJpa.context(database.dataSource(), AllowedGuildPersistenceAdapter.class);
        repository = context.getBean(AllowedGuildPersistenceAdapter.class);
        transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
    }

    @AfterEach
    void tearDown() {
        // Docker 가 없어 MySQL 쪽을 건너뛰면 문맥을 만들기 전에 끝난다.
        if (context != null) {
            context.close();
        }
    }

    @Test
    void 넣은_순서대로_돌려준다() {
        repository.saveIfAbsent(AllowedGuild.allow(SECOND, LATER));
        repository.saveIfAbsent(AllowedGuild.allow(FIRST, EARLIER));

        assertThat(repository.findAll()).extracting(AllowedGuild::guildId, AllowedGuild::allowedAt)
                .containsExactly(
                        tuple(FIRST, EARLIER),
                        tuple(SECOND, LATER));
    }

    @Test
    void 이미_있는_서버는_다시_넣지_않는다() {
        assertThat(repository.saveIfAbsent(AllowedGuild.allow(FIRST, EARLIER))).isTrue();
        assertThat(repository.saveIfAbsent(AllowedGuild.allow(FIRST, LATER))).isFalse();

        assertThat(repository.findAll()).extracting(AllowedGuild::allowedAt).containsExactly(EARLIER);
    }

    @Test
    void 한꺼번에_넣는다() {
        repository.saveAll(List.of(AllowedGuild.allow(FIRST, EARLIER), AllowedGuild.allow(SECOND, EARLIER)));

        assertThat(repository.findAll()).extracting(AllowedGuild::guildId).containsExactlyInAnyOrder(FIRST, SECOND);
    }

    @Test
    void 트랜잭션_안에서_한꺼번에_넣다가_하나라도_실패하면_하나도_넣지_않는다() {
        repository.saveIfAbsent(AllowedGuild.allow(SECOND, EARLIER));

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> repository.saveAll(
                List.of(AllowedGuild.allow(FIRST, LATER), AllowedGuild.allow(SECOND, LATER)))))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 일부만 남으면 다음에 켤 때 목록이 비어 있지 않아서 남은 디스코드 서버에서 나가게 된다.
        assertThat(repository.findAll()).extracting(AllowedGuild::guildId).containsExactly(SECOND);
    }

    @Test
    void 지우면_없다() {
        repository.saveIfAbsent(AllowedGuild.allow(FIRST, EARLIER));

        assertThat(repository.delete(FIRST)).isTrue();
        assertThat(repository.delete(FIRST)).isFalse();
        assertThat(repository.findAll()).isEmpty();
    }
}
