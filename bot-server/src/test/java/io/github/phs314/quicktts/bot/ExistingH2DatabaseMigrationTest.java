package io.github.phs314.quicktts.bot;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.adapter.out.persistence.JdbcDeviceRepository;
import io.github.phs314.quicktts.bot.guild.adapter.out.persistence.JdbcAllowedGuildRepository;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.adapter.out.persistence.JdbcVoicePreferenceRepository;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.support.TestDatabase;
import java.time.Instant;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Flyway 를 넣기 전에는 서버가 켜질 때마다 schema.sql 을 돌렸다. 그렇게 만든 H2 파일 DB(개발용 data/, exe 의
 * ~/.discord-quick-tts/data/)를 지우지 않고 그대로 이어 쓰는지 본다.
 */
class ExistingH2DatabaseMigrationTest {

    private static final DiscordUserId OWNER = new DiscordUserId(123456789012345678L);
    private static final GuildId GUILD = new GuildId(987654321098765432L);

    @Test
    void 허용_목록_전에_만든_DB_는_V1_을_건너뛰고_허용_목록_테이블을_더한다() {
        DataSource dataSource = TestDatabase.H2.emptyDataSource();
        JdbcClient jdbc = JdbcClient.create(dataSource);
        createTablesLikeOldSchemaSql(jdbc);
        insertDeviceAndVoice(jdbc);

        TestDatabase.migrate(dataSource);

        assertThat(historyType(jdbc, "1")).isEqualTo("BASELINE");
        assertThat(historyType(jdbc, "2")).isEqualTo("SQL");
        assertKeepsDeviceAndVoice(jdbc);
        var allowlist = new JdbcAllowedGuildRepository(jdbc);
        allowlist.add(AllowedGuild.allow(GUILD, Instant.parse("2026-10-11T00:00:00Z")));
        assertThat(allowlist.exists(GUILD)).isTrue();
    }

    @Test
    void 허용_목록_테이블까지_있던_DB_는_목록을_그대로_둔다() {
        DataSource dataSource = TestDatabase.H2.emptyDataSource();
        JdbcClient jdbc = JdbcClient.create(dataSource);
        createTablesLikeOldSchemaSql(jdbc);
        jdbc.sql("""
                create table allowed_guild (
                    guild_id   bigint    primary key,
                    allowed_at timestamp not null
                )""").update();
        jdbc.sql("insert into allowed_guild values (:guild, current_timestamp)")
                .param("guild", GUILD.value())
                .update();
        insertDeviceAndVoice(jdbc);

        TestDatabase.migrate(dataSource);

        assertKeepsDeviceAndVoice(jdbc);
        assertThat(new JdbcAllowedGuildRepository(jdbc).exists(GUILD)).isTrue();
    }

    private static void insertDeviceAndVoice(JdbcClient jdbc) {
        jdbc.sql("insert into device (token_hash, discord_user_id, registered_at, id, name)"
                        + " values (:hash, :owner, current_timestamp, 'old-device-id', '현수-PC')")
                .param("hash", "a".repeat(64))
                .param("owner", OWNER.value())
                .update();
        jdbc.sql("insert into voice_preference values (:owner, 'edge:ko-KR-SunHiNeural')")
                .param("owner", OWNER.value())
                .update();
    }

    private static void assertKeepsDeviceAndVoice(JdbcClient jdbc) {
        assertThat(new JdbcDeviceRepository(jdbc).findByOwner(OWNER))
                .singleElement()
                .satisfies(device -> assertThat(device.id().value()).isEqualTo("old-device-id"));
        var voices = new JdbcVoicePreferenceRepository(jdbc);
        voices.save(OWNER, new VoiceId("edge:ko-KR-InJoonNeural"));
        assertThat(voices.find(OWNER)).contains(new VoiceId("edge:ko-KR-InJoonNeural"));
    }

    private static String historyType(JdbcClient jdbc, String version) {
        return jdbc.sql("select \"type\" from \"flyway_schema_history\" where \"version\" = :version")
                .param("version", version)
                .query(String.class)
                .single();
    }

    /** 허용 목록이 생기기 전, 지워진 schema.sql 을 마지막으로 돌린 뒤의 모양. */
    private static void createTablesLikeOldSchemaSql(JdbcClient jdbc) {
        jdbc.sql("""
                create table device (
                    token_hash      varchar(64) primary key,
                    discord_user_id bigint      not null,
                    registered_at   timestamp   not null
                )""").update();
        jdbc.sql("alter table device add column id varchar(36)").update();
        jdbc.sql("alter table device add column name varchar(50)").update();
        jdbc.sql("create unique index ux_device_id on device (id)").update();
        jdbc.sql("create index idx_device_discord_user_id on device (discord_user_id)").update();
        jdbc.sql("""
                create table voice_preference (
                    discord_user_id bigint       primary key,
                    voice_id        varchar(100) not null
                )""").update();
    }
}
