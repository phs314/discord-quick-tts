package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** 사용자별 목소리 저장소 대신 쓰는 메모리 대역. */
public class FakeVoicePreferencePort implements VoicePreferencePort {

    private final Map<DiscordUserId, VoiceId> byUser = new HashMap<>();

    /** 지금까지 저장된 사용자별 목소리. */
    public Map<DiscordUserId, VoiceId> saved() {
        return Map.copyOf(byUser);
    }

    @Override
    public Optional<VoiceId> findVoice(DiscordUserId user) {
        return Optional.ofNullable(byUser.get(user));
    }

    @Override
    public void saveVoice(DiscordUserId user, VoiceId voiceId) {
        byUser.put(user, voiceId);
    }
}
