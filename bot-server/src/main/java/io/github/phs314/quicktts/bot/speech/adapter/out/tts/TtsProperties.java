package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * TTS 엔진 설정 ({@code quicktts.tts}). TTS 어댑터만 쓰므로 이 패키지에 둔다.
 * 기본값은 application.yml 에 있고, 값이 비면 켜질 때 멈춘다.
 *
 * @param defaultVoice        목소리를 고르지 않은 사용자에게 쓰는 목소리
 * @param edgeChromiumVersion Edge 목소리 서버가 확인하는 Edge 브라우저 버전. Edge 목소리가 갑자기 안 되면 최신 버전으로 올린다.
 */
@Validated
@ConfigurationProperties("quicktts.tts")
public record TtsProperties(
        @NotBlank(message = "QUICKTTS_DEFAULT_VOICE 가 비어 있습니다. 목소리 식별자를 적거나 줄을 지워 기본 목소리를 쓰세요.")
        String defaultVoice,
        @NotBlank(message = "quicktts.tts.edge-chromium-version 이 비어 있습니다. application.yml 에 Edge 브라우저 버전을 적으세요.")
        String edgeChromiumVersion) {
}
