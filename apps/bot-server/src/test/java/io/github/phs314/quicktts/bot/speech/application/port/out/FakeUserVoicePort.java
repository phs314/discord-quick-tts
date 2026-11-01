package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.HashMap;
import java.util.Map;

/** 사용자 설정을 묻는 대신 쓰는 대역. 목소리를 정해 두지 않은 사용자는 {@link #DEFAULT_VOICE} 로 읽는다. */
public class FakeUserVoicePort implements UserVoicePort {

    public static final VoiceId DEFAULT_VOICE = new VoiceId("fake:sunhi");

    private final Map<DiscordUserId, VoiceId> byUser = new HashMap<>();

    public void choose(DiscordUserId user, VoiceId voiceId) {
        byUser.put(user, voiceId);
    }

    @Override
    public VoiceId getVoiceId(DiscordUserId user) {
        return byUser.getOrDefault(user, DEFAULT_VOICE);
    }
}
