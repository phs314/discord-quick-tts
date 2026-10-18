package io.github.phs314.quicktts.bot.speech.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link VoicePreferenceJpaEntity} 를 읽고 쓰는 Spring Data 저장소. {@link JpaVoicePreferenceRepository} 만 쓴다.
 */
interface SpringDataVoicePreferenceRepository extends JpaRepository<VoicePreferenceJpaEntity, Long> {
}
