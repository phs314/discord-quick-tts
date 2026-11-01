package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import java.nio.charset.StandardCharsets;

/** synthesis 대신 쓰는 대역. 바깥 서비스를 부르지 않고 "목소리:문장" 글을 음성 바이트로 돌려준다. */
public class FakeTextToSpeechPort implements TextToSpeechPort {

    @Override
    public Audio synthesize(SpokenText text, VoiceId voiceId) {
        return new Audio((voiceId.value() + ":" + text.text()).getBytes(StandardCharsets.UTF_8), "mp3");
    }
}
