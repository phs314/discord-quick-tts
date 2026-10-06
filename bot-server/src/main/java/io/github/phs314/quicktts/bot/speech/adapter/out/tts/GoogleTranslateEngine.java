package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Google 번역의 공개 TTS 엔드포인트를 쓰는 무료 엔진. 한국어 목소리 하나뿐이다.
 * 공식 API 가 아니라 언제든 막힐 수 있지만, 다른 엔진이 실패할 때 대신 읽어 주는 예비 목소리로도 쓴다.
 */
@Component
@Order(2)
public class GoogleTranslateEngine implements TtsEngine {

    static final VoiceId VOICE_ID = new VoiceId("google:ko");

    private static final String ENDPOINT = "https://translate.google.com/translate_tts";
    private static final List<Voice> VOICES = List.of(new Voice(VOICE_ID, "구글 번역 (예전 목소리)"));

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public List<Voice> voices() {
        return VOICES;
    }

    @Override
    public Speech synthesize(String text, VoiceId voice) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(text, "ko"))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new SpeechSynthesisException("Google 번역 TTS 응답 코드가 " + response.statusCode() + " 입니다.");
            }
            return new Speech(response.body(), "mp3");
        } catch (IOException e) {
            throw new SpeechSynthesisException("Google 번역 TTS 호출에 실패했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpeechSynthesisException("Google 번역 TTS 호출이 중단되었습니다.", e);
        }
    }

    static URI buildUri(String text, String language) {
        String query = "ie=UTF-8&client=tw-ob"
                + "&tl=" + URLEncoder.encode(language, StandardCharsets.UTF_8)
                + "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        return URI.create(ENDPOINT + "?" + query);
    }
}
