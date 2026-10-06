package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * 사용자가 고른 목소리를 {@code voice_preference} 테이블에 저장한다. 스키마는 {@code schema.sql} 에 있다.
 */
@Component
public class JdbcVoicePreferenceRepository implements VoicePreferencePort {

    private final JdbcClient jdbcClient;

    public JdbcVoicePreferenceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<VoiceId> find(DiscordUserId user) {
        return jdbcClient.sql("select voice_id from voice_preference where discord_user_id = :user")
                .param("user", user.value())
                .query((rs, rowNum) -> new VoiceId(rs.getString("voice_id")))
                .optional();
    }

    @Override
    public void save(DiscordUserId user, VoiceId voice) {
        jdbcClient.sql("merge into voice_preference (discord_user_id, voice_id) key (discord_user_id)"
                        + " values (:user, :voice)")
                .param("user", user.value())
                .param("voice", voice.value())
                .update();
    }
}
