package io.github.phs314.quicktts.bot.identity.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.identity.domain.Pairing;
import io.github.phs314.quicktts.bot.identity.domain.vo.PairingCode;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import io.github.phs314.quicktts.bot.support.TestJpa;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ParameterizedClass
@EnumSource(TestDatabase.class)
class PairingPersistenceAdapterTest {

    private static final DiscordUserId USER = new DiscordUserId(123456789012345678L);
    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Parameter
    TestDatabase database;

    private AnnotationConfigApplicationContext context;
    private PairingPersistenceAdapter repository;

    @BeforeEach
    void setUp() {
        context = TestJpa.context(database.dataSource(), FixedClock.class, PairingPersistenceAdapter.class);
        repository = context.getBean(PairingPersistenceAdapter.class);
    }

    @AfterEach
    void tearDown() {
        // Docker 가 없어 MySQL 쪽을 건너뛰면 문맥을 만들기 전에 끝난다.
        if (context != null) {
            context.close();
        }
    }

    @Test
    void 저장한_연결은_한_번만_꺼내진다() {
        Pairing pairing = Pairing.issue(USER, NOW);

        repository.save(pairing);

        assertThat(repository.findAndDelete(pairing.code())).get()
                .usingRecursiveComparison().isEqualTo(pairing);
        assertThat(repository.findAndDelete(pairing.code())).isEmpty();
    }

    @Test
    void 없는_코드는_비어_있다() {
        assertThat(repository.findAndDelete(PairingCode.generate())).isEmpty();
    }

    @Test
    void 저장할_때_만료된_연결을_지운다() {
        Pairing expired = Pairing.restore(PairingCode.generate(), USER, NOW.minus(1, ChronoUnit.MINUTES));
        Pairing valid = Pairing.restore(PairingCode.generate(), USER, NOW.plus(1, ChronoUnit.MINUTES));
        repository.save(expired);

        repository.save(valid);

        assertThat(repository.findAndDelete(expired.code())).isEmpty();
        assertThat(repository.findAndDelete(valid.code())).isPresent();
    }

    @Configuration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
