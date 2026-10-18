package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.adapter.in.event.GuildDisallowedListener;
import io.github.phs314.quicktts.bot.guild.adapter.out.event.SpringGuildAllowlistEvent;
import io.github.phs314.quicktts.bot.guild.adapter.out.persistence.JdbcAllowedGuildRepository;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 허용 목록 유스케이스의 트랜잭션 경계와 커밋 뒤 이벤트를 실제 DB 와 스프링 트랜잭션으로 확인한다 (ADR 0033).
 * 다른 서비스 테스트처럼 new 로 만들면 {@code @Transactional} 이 걸리지 않아서 스프링 문맥을 작게 띄운다.
 * 테스트마다 문맥을 새로 띄워서 빈 DB 로 시작한다.
 */
@SpringJUnitConfig(GuildAllowlistTransactionTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GuildAllowlistTransactionTest {

    private static final Instant NOW = Instant.parse("2026-10-18T00:00:00Z");
    private static final DiscordUserId OWNER = new DiscordUserId(100L);
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId STRANGER = new GuildId(2L);

    @Autowired
    EnforceGuildAllowlistUseCase enforceGuildAllowlistUseCase;

    @Autowired
    ManageGuildAllowlistUseCase manageGuildAllowlistUseCase;

    @Autowired
    JdbcAllowedGuildRepository repository;

    @Autowired
    TransactionRecordingJoinedGuildPort joinedGuildPort;

    @Test
    void 처음_채우다_실패하면_하나도_넣지_않는다() {
        // 같은 디스코드 서버가 두 번 나오게 해서, 앞의 둘을 넣은 뒤 세 번째 넣기가 DB 오류(중복 키)로 실패하게 한다.
        joinedGuildPort.join(MINE);
        joinedGuildPort.join(STRANGER);
        joinedGuildPort.join(MINE);

        assertThatThrownBy(enforceGuildAllowlistUseCase::fillIfEmpty).isInstanceOf(DuplicateKeyException.class);

        // 일부만 남았다면 다음에 켤 때 목록이 비어 있지 않아서 남은 디스코드 서버에서 나가게 된다.
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void 디스코드_서버에서_나가기는_트랜잭션_밖에서_한다() {
        repository.saveIfAbsent(AllowedGuild.allow(MINE, NOW));
        joinedGuildPort.join(MINE);
        joinedGuildPort.join(STRANGER);

        enforceGuildAllowlistUseCase.fillIfEmpty();
        enforceGuildAllowlistUseCase.leaveAllNotAllowed();

        assertThat(joinedGuildPort.left()).containsExactly(STRANGER);
        assertThat(joinedGuildPort.leftInTransaction()).containsExactly(false);
    }

    @Test
    void 허용_목록에서_빼면_커밋한_뒤에_그_서버에서_나간다() {
        repository.saveIfAbsent(AllowedGuild.allow(MINE, NOW));
        repository.saveIfAbsent(AllowedGuild.allow(STRANGER, NOW));
        joinedGuildPort.join(MINE);
        joinedGuildPort.join(STRANGER);

        manageGuildAllowlistUseCase.disallow(new DisallowGuildCommand(OWNER, STRANGER));

        assertThat(repository.findAll()).extracting(AllowedGuild::guildId).containsExactly(MINE);
        assertThat(joinedGuildPort.left()).containsExactly(STRANGER);
        // 커밋 뒤라서, 나가는 순간 다른 연결에서도 이미 목록에서 빠진 것이 보인다.
        assertThat(joinedGuildPort.stillAllowedForOthers()).containsExactly(false);
    }

    /**
     * 나갈 때마다 그때 트랜잭션이 열려 있었는지와, 트랜잭션 밖의 새 연결에서 그 서버가 아직 허용 목록에 보이는지 적어 둔다.
     * 커밋 뒤 이벤트를 받는 동안에는 스프링이 트랜잭션을 아직 정리하지 않아서 열린 것으로 보이므로, 커밋됐는지는 새 연결로 본다.
     */
    static class TransactionRecordingJoinedGuildPort extends FakeJoinedGuildPort {

        private final DataSource dataSource;
        private final List<Boolean> leftInTransaction = new ArrayList<>();
        private final List<Boolean> stillAllowedForOthers = new ArrayList<>();

        TransactionRecordingJoinedGuildPort(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public void leaveWithNotice(GuildId guildId) {
            leftInTransaction.add(TransactionSynchronizationManager.isActualTransactionActive());
            stillAllowedForOthers.add(isAllowedSeenFromNewConnection(guildId));
            super.leaveWithNotice(guildId);
        }

        private boolean isAllowedSeenFromNewConnection(GuildId guildId) {
            try (Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "select count(*) from allowed_guild where guild_id = ?")) {
                statement.setLong(1, guildId.value());
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getLong(1) > 0;
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        List<Boolean> leftInTransaction() {
            return List.copyOf(leftInTransaction);
        }

        List<Boolean> stillAllowedForOthers() {
            return List.copyOf(stillAllowedForOthers);
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {

        @Bean
        DataSource dataSource() {
            return TestDatabase.H2.dataSource();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new JdbcTransactionManager(dataSource);
        }

        @Bean
        JdbcAllowedGuildRepository repository(DataSource dataSource) {
            return new JdbcAllowedGuildRepository(JdbcClient.create(dataSource));
        }

        @Bean
        TransactionRecordingJoinedGuildPort joinedGuildPort(DataSource dataSource) {
            return new TransactionRecordingJoinedGuildPort(dataSource);
        }

        @Bean
        EnforceGuildAllowlistService enforceGuildAllowlistService(
                JdbcAllowedGuildRepository repository, TransactionRecordingJoinedGuildPort joinedGuildPort) {
            return new EnforceGuildAllowlistService(repository, joinedGuildPort,
                    new FakeGuildAllowlistSettingPort(), Clock.fixed(NOW, ZoneOffset.UTC));
        }

        @Bean
        ManageGuildAllowlistService manageGuildAllowlistService(
                JdbcAllowedGuildRepository repository, ApplicationEventPublisher applicationEventPublisher) {
            return new ManageGuildAllowlistService(() -> OWNER, repository, new FakeGuildAllowlistSettingPort(),
                    new SpringGuildAllowlistEvent(applicationEventPublisher), Clock.fixed(NOW, ZoneOffset.UTC));
        }

        @Bean
        GuildDisallowedListener guildDisallowedListener(EnforceGuildAllowlistUseCase enforceGuildAllowlistUseCase) {
            return new GuildDisallowedListener(enforceGuildAllowlistUseCase);
        }
    }
}
