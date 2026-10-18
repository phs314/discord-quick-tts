package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자가 고른 목소리를 {@code voice_preference} 테이블에 저장한다. 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class JpaVoicePreferenceRepository implements VoicePreferencePort {

    private final SpringDataVoicePreferenceRepository springDataVoicePreferenceRepository;

    @Override
    public Optional<VoiceId> findVoice(DiscordUserId user) {
        return springDataVoicePreferenceRepository.findById(user.value())
                .map(VoicePreferencePersistenceMapper::toVoiceId);
    }

    /** 기본 키가 사용자라서, 고른 적이 있으면 JPA 가 그 줄을 고치고 없으면 넣는다. */
    @Override
    public void saveVoice(DiscordUserId user, VoiceId voiceId) {
        springDataVoicePreferenceRepository.save(VoicePreferencePersistenceMapper.toEntity(user, voiceId));
    }
}
