package io.github.phs314.quicktts.bot.speech.application.port.in.usecase;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.List;

/**
 * 사용자가 자기 문장을 읽어 줄 목소리를 보고 바꾼다.
 */
public interface ManageVoiceUseCase {

    List<Voice> voices();

    /** 고른 목소리. 고른 적이 없거나 더 이상 없는 목소리면 기본 목소리. */
    Voice currentVoice(DiscordUserId user);

    /**
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.UnknownVoiceException 없는 목소리일 때
     */
    Voice changeVoice(DiscordUserId user, VoiceId voice);
}
