package io.github.phs314.quicktts.bot.adapter.out.tts;

import io.github.phs314.quicktts.bot.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.config.QuickTtsProperties;
import io.github.phs314.quicktts.bot.domain.QuickChatMessage;
import io.github.phs314.quicktts.bot.domain.Speech;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Google 번역의 공개 TTS 엔드포인트를 쓰는 무료 엔진.
 * 공식 API 가 아니므로 언제든 막힐 수 있다. 그때는 다른 {@link SpeechSynthesizer} 어댑터로 바꾼다.
 */
@Component
@ConditionalOnProperty(name = "quicktts.tts.engine", havingValue = "google-translate", matchIfMissing = true)
public class GoogleTranslateSpeechSynthesizer implements SpeechSynthesizer {

    private static final String ENDPOINT = "https://translate.google.com/translate_tts";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String language;

    public GoogleTranslateSpeechSynthesizer(QuickTtsProperties properties) {
        this.language = properties.tts().language();
    }

    @Override
    public Speech synthesize(QuickChatMessage message) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(message.text(), language))
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
