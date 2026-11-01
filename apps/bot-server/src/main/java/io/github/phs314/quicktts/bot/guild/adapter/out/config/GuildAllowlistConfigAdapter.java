package io.github.phs314.quicktts.bot.guild.adapter.out.config;

import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistConfigPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 허용 목록을 쓸지를 설정 값 {@code quicktts.guild-allowlist.enabled} 에서 읽는다.
 */
@Component
@RequiredArgsConstructor
public class GuildAllowlistConfigAdapter implements GuildAllowlistConfigPort {

    private final GuildAllowlistProperties properties;

    @Override
    public boolean isEnabled() {
        return properties.enabled();
    }
}
