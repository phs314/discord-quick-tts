package io.github.phs314.quicktts.bot.settings.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.settings.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.settings.application.port.out.FakeUserSettingsPort;
import io.github.phs314.quicktts.bot.settings.application.port.out.FakeVoiceChoicesPort;
import io.github.phs314.quicktts.bot.settings.domain.UnknownVoiceException;
import io.github.phs314.quicktts.bot.settings.domain.UserSettings;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import org.junit.jupiter.api.Test;

class ChangeVoiceServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);

    private final FakeUserSettingsPort settings = new FakeUserSettingsPort();
    private final ChangeVoiceService service = new ChangeVoiceService(new FakeVoiceChoicesPort(), settings);

    @Test
    void 바꾼_목소리를_돌려주고_저장한다() {
        assertThat(service.changeVoice(new ChangeVoiceCommand(USER, FakeVoiceChoicesPort.INJOON.id())))
                .isEqualTo(FakeVoiceChoicesPort.INJOON);
        assertThat(settings.find(USER).flatMap(UserSettings::voiceId)).contains(FakeVoiceChoicesPort.INJOON.id());
    }

    @Test
    void 다시_바꾸면_마지막_목소리가_남는다() {
        service.changeVoice(new ChangeVoiceCommand(USER, FakeVoiceChoicesPort.INJOON.id()));
        service.changeVoice(new ChangeVoiceCommand(USER, FakeVoiceChoicesPort.SUNHI.id()));

        assertThat(settings.find(USER).flatMap(UserSettings::voiceId)).contains(FakeVoiceChoicesPort.SUNHI.id());
    }

    @Test
    void 목록에_없는_목소리로는_바꿀_수_없다() {
        assertThatThrownBy(() -> service.changeVoice(new ChangeVoiceCommand(USER, new VoiceId("fake:없음"))))
                .isInstanceOf(UnknownVoiceException.class);
        assertThat(settings.saved()).isEmpty();
    }
}
