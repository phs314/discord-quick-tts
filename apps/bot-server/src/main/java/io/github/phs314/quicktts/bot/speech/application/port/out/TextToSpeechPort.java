package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;

/**
 * 다듬은 문장을 음성으로 만든다. 엔진과 목소리 목록은 synthesis 가 맡는다.
 */
public interface TextToSpeechPort {

    /**
     * 음성을 만들지 못하면 {@code BusinessException}(종류 {@code UNAVAILABLE}) 이 그대로 올라간다.
     */
    Audio synthesize(SpokenText text, VoiceId voiceId);
}
