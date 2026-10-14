package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TTS 엔진 설정 ({@code quicktts.tts}). TTS 어댑터만 쓰므로 이 패키지에 둔다.
 *
 * @param defaultVoice        목소리를 고르지 않은 사용자에게 쓰는 목소리
 * @param edgeChromiumVersion Edge 목소리 서버가 확인하는 Edge 브라우저 버전
 */
@ConfigurationProperties("quicktts.tts")
public record TtsProperties(String defaultVoice, String edgeChromiumVersion) {

    public TtsProperties {
        if (defaultVoice == null || defaultVoice.isBlank()) {
            defaultVoice = "edge:ko-KR-SunHiNeural";
        }
        if (edgeChromiumVersion == null || edgeChromiumVersion.isBlank()) {
            edgeChromiumVersion = "143.0.3650.75";
        }
    }
}
