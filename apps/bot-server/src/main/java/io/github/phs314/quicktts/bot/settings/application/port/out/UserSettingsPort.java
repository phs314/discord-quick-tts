package io.github.phs314.quicktts.bot.settings.application.port.out;

import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.Optional;

/**
 * 사용자 설정을 기억한다.
 */
public interface UserSettingsPort {

    /** 아무것도 바꾼 적 없는 사용자면 비어 있다. */
    Optional<UserSettings> find(DiscordUserId user);

    void save(UserSettings settings);
}
