package io.github.phs314.quicktts.bot.settings.domain;

import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.Objects;
import java.util.Optional;

/**
 * 디스코드 사용자 한 명이 고른 설정 (디스코드의 User Settings). 지금은 내 문장을 읽어 줄 목소리 하나다.
 * 같은 사용자인지는 {@link #userId()} 로만 가린다.
 */
public class UserSettings {

    /** 설정의 주인 */
    private final DiscordUserId userId;
    /** 고른 목소리. 고른 적이 없으면 null 이고, 그때는 기본 목소리로 읽는다. */
    private VoiceId voiceId;

    /** 저장소에서 읽어 온 설정을 되살릴 때 쓴다. 처음 바꾸는 사용자는 {@link #defaults} 로 만든다. */
    public UserSettings(DiscordUserId userId, VoiceId voiceId) {
        this.userId = Objects.requireNonNull(userId);
        this.voiceId = voiceId;
    }

    /** 아무것도 고르지 않은 사용자의 설정 */
    public static UserSettings defaults(DiscordUserId userId) {
        return new UserSettings(userId, null);
    }

    /** 목소리를 바꾼다. 고를 수 있는 목록({@link io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices#choose})에서 고른 것만 받는다. */
    public void changeVoice(VoiceChoice voice) {
        this.voiceId = voice.id();
    }

    public DiscordUserId userId() {
        return userId;
    }

    public Optional<VoiceId> voiceId() {
        return Optional.ofNullable(voiceId);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserSettings other && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "UserSettings[userId=" + userId + ", voiceId=" + voiceId + "]";
    }
}
