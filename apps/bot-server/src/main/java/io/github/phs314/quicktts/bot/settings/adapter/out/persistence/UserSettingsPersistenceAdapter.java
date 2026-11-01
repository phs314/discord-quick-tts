package io.github.phs314.quicktts.bot.settings.adapter.out.persistence;

import io.github.phs314.quicktts.bot.settings.application.port.out.UserSettingsPort;
import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 설정을 {@code user_settings} 테이블에 저장한다. 스키마는 {@code db/migration} 에 있다.
 */
@Component
@RequiredArgsConstructor
public class UserSettingsPersistenceAdapter implements UserSettingsPort {

    private final UserSettingsSpringDataRepository userSettingsSpringDataRepository;

    @Override
    public Optional<UserSettings> find(DiscordUserId user) {
        return userSettingsSpringDataRepository.findById(user.value()).map(UserSettingsPersistenceMapper::toDomain);
    }

    /** 기본 키가 사용자라서, 저장한 적이 있으면 JPA 가 그 줄을 고치고 없으면 넣는다. */
    @Override
    public void save(UserSettings settings) {
        userSettingsSpringDataRepository.save(UserSettingsPersistenceMapper.toEntity(settings));
    }
}
