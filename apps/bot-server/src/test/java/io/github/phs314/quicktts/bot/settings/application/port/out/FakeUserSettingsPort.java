package io.github.phs314.quicktts.bot.settings.application.port.out;

import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** 사용자 설정 저장소 대신 쓰는 메모리 대역. 저장소처럼 꺼낼 때마다 새 객체를 돌려준다. */
public class FakeUserSettingsPort implements UserSettingsPort {

    private final Map<DiscordUserId, UserSettings> byUser = new HashMap<>();

    /** 지금까지 저장된 사용자 설정. */
    public Map<DiscordUserId, UserSettings> saved() {
        return Map.copyOf(byUser);
    }

    @Override
    public Optional<UserSettings> find(DiscordUserId user) {
        return Optional.ofNullable(byUser.get(user))
                .map(settings -> new UserSettings(settings.userId(), settings.voiceId().orElse(null)));
    }

    @Override
    public void save(UserSettings settings) {
        byUser.put(settings.userId(), new UserSettings(settings.userId(), settings.voiceId().orElse(null)));
    }
}
