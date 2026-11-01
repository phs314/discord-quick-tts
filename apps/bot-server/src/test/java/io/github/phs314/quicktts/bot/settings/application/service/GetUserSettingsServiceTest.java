package io.github.phs314.quicktts.bot.settings.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.settings.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.settings.application.port.in.dto.UserSettingsDto;
import io.github.phs314.quicktts.bot.settings.application.port.in.query.GetUserSettingsQuery;
import io.github.phs314.quicktts.bot.settings.application.port.out.FakeUserSettingsPort;
import io.github.phs314.quicktts.bot.settings.application.port.out.FakeVoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import org.junit.jupiter.api.Test;

class GetUserSettingsServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);

    private final FakeVoiceChoicesPort voices = new FakeVoiceChoicesPort();
    private final FakeUserSettingsPort settings = new FakeUserSettingsPort();
    private final GetUserSettingsService service = new GetUserSettingsService(voices, settings);

    @Test
    void 고른_적이_없으면_기본_목소리다() {
        assertThat(service.getUserSettings(new GetUserSettingsQuery(USER)))
                .isEqualTo(new UserSettingsDto(USER, FakeVoiceChoicesPort.SUNHI.id(), FakeVoiceChoicesPort.SUNHI.label()));
    }

    @Test
    void 목소리를_바꾸면_다음부터_그_목소리다() {
        new ChangeVoiceService(voices, settings).changeVoice(new ChangeVoiceCommand(USER, FakeVoiceChoicesPort.INJOON.id()));

        assertThat(service.getUserSettings(new GetUserSettingsQuery(USER)).voiceId()).isEqualTo(FakeVoiceChoicesPort.INJOON.id());
    }

    @Test
    void 골랐던_목소리가_목록에서_사라졌으면_기본_목소리로_읽는다() {
        settings.save(new UserSettings(USER, new VoiceId("fake:사라진-목소리")));

        assertThat(service.getUserSettings(new GetUserSettingsQuery(USER)).voiceId()).isEqualTo(FakeVoiceChoicesPort.SUNHI.id());
    }
}
