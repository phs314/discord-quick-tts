package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.adapter.out.persistence.JdbcAllowedGuildRepository;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 허용 목록을 처음 채우다 실패하면 아무것도 남지 않는지, 실제 DB 와 스프링 트랜잭션으로 확인한다.
 * 다른 서비스 테스트처럼 new 로 만들면 {@code @Transactional} 이 걸리지 않아서 스프링 문맥을 작게 띄운다.
 */
@SpringJUnitConfig(EnforceGuildAllowlistTransactionTest.Config.class)
class EnforceGuildAllowlistTransactionTest {

    private static final Instant NOW = Instant.parse("2026-10-14T00:00:00Z");

    @Autowired
    EnforceGuildAllowlistUseCase enforceGuildAllowlistUseCase;

    @Autowired
    JdbcAllowedGuildRepository repository;

    @Test
    void 처음_채우다_실패하면_하나도_넣지_않는다() {
        assertThatThrownBy(enforceGuildAllowlistUseCase::leaveAllNotAllowed)
                .isInstanceOf(IllegalStateException.class);

        // 일부만 남았다면 다음에 켤 때 목록이 비어 있지 않아서 남은 디스코드 서버에서 나가게 된다.
        assertThat(repository.findAll()).isEmpty();
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
        EnforceGuildAllowlistService enforceGuildAllowlistService(JdbcAllowedGuildRepository repository) {
            return new EnforceGuildAllowlistService(
                    failingOnSecondAdd(repository),
                    new FakeJoinedGuildPort(new GuildId(1L), new GuildId(2L)),
                    new FakeGuildAllowlistSettingPort(),
                    Clock.fixed(NOW, ZoneOffset.UTC));
        }

        /** 첫 디스코드 서버는 DB 에 실제로 넣고, 두 번째에서 DB 오류가 난 것처럼 실패한다. */
        private static AllowedGuildPort failingOnSecondAdd(AllowedGuildPort delegate) {
            AtomicInteger adds = new AtomicInteger();
            return new AllowedGuildPort() {
                @Override
                public List<AllowedGuild> findAll() {
                    return delegate.findAll();
                }

                @Override
                public boolean add(AllowedGuild allowedGuild) {
                    if (adds.incrementAndGet() == 2) {
                        throw new IllegalStateException("DB 오류");
                    }
                    return delegate.add(allowedGuild);
                }

                @Override
                public boolean delete(GuildId guildId) {
                    return delegate.delete(guildId);
                }
            };
        }
    }
}
