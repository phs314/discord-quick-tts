package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import io.github.phs314.quicktts.bot.config.QuickTtsProperties;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesisException;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Microsoft Edge 의 "소리 내어 읽기" 목소리를 쓰는 무료 엔진.
 * 공식 API 가 아니라 Edge 가 쓰는 웹소켓 프로토콜을 흉내 낸다(edge-tts 오픈소스 구현 참고).
 * 서버가 Edge 버전을 확인하므로, 갑자기 안 되면 {@code quicktts.tts.edge-chromium-version} 을 최신으로 올린다.
 */
@Component
@Order(1)
public class EdgeReadAloudEngine implements TtsEngine {

    private static final String PREFIX = "edge:";
    private static final List<Voice> VOICES = List.of(
            voice("ko-KR-SunHiNeural", "선희 (여성)"),
            voice("ko-KR-InJoonNeural", "인준 (남성)"),
            voice("ko-KR-HyunsuMultilingualNeural", "현수 (남성)"));

    private static final String TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4";
    private static final String ENDPOINT =
            "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1";
    /** Windows 파일 시간(1601-01-01) 과 유닉스 시간(1970-01-01) 의 차이(초). */
    private static final long WINDOWS_EPOCH_OFFSET_SECONDS = 11_644_473_600L;
    private static final DateTimeFormatter JS_DATE = DateTimeFormatter
            .ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT+0000 (Coordinated Universal Time)'", Locale.US)
            .withZone(ZoneOffset.UTC);
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String chromiumVersion;
    private final Clock clock;

    public EdgeReadAloudEngine(QuickTtsProperties properties, Clock clock) {
        this.chromiumVersion = properties.tts().edgeChromiumVersion();
        this.clock = clock;
    }

    @Override
    public List<Voice> voices() {
        return VOICES;
    }

    @Override
    public Speech synthesize(String text, VoiceId voice) {
        Instant now = clock.instant();
        AudioCollector collector = new AudioCollector();
        WebSocket webSocket = null;
        try {
            webSocket = httpClient.newWebSocketBuilder()
                    .header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
                    .header("User-Agent", userAgent())
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .buildAsync(connectUri(now), collector)
                    .get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            webSocket.sendText(configMessage(now), true).join();
            webSocket.sendText(ssmlMessage(now, voiceName(voice), text), true).join();
            byte[] audio = collector.result().get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (audio.length == 0) {
                throw new SpeechSynthesisException("Edge TTS 가 음성을 보내지 않았습니다.");
            }
            return new Speech(audio, "mp3");
        } catch (ExecutionException | TimeoutException | CompletionException e) {
            throw new SpeechSynthesisException("Edge TTS 호출에 실패했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpeechSynthesisException("Edge TTS 호출이 중단되었습니다.", e);
        } finally {
            if (webSocket != null) {
                webSocket.abort();
            }
        }
    }

    private URI connectUri(Instant now) {
        return URI.create(ENDPOINT
                + "?TrustedClientToken=" + TRUSTED_CLIENT_TOKEN
                + "&ConnectionId=" + randomId()
                + "&Sec-MS-GEC=" + secMsGec(now)
                + "&Sec-MS-GEC-Version=1-" + chromiumVersion);
    }

    private String userAgent() {
        String major = chromiumVersion.split("\\.", 2)[0];
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/" + major + ".0.0.0 Safari/537.36 Edg/" + major + ".0.0.0";
    }

    /** 서버가 요구하는 시간 기반 토큰. 5분 단위로 내린 Windows 파일 시간에 클라이언트 토큰을 붙여 SHA-256 한다. */
    static String secMsGec(Instant now) {
        long seconds = now.getEpochSecond() + WINDOWS_EPOCH_OFFSET_SECONDS;
        seconds -= seconds % 300;
        String ticks = seconds + "0000000";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((ticks + TRUSTED_CLIENT_TOKEN).getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().withUpperCase().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 을 쓸 수 없습니다.", e);
        }
    }

    private static String configMessage(Instant now) {
        return "X-Timestamp:" + JS_DATE.format(now) + "\r\n"
                + "Content-Type:application/json; charset=utf-8\r\n"
                + "Path:speech.config\r\n\r\n"
                + "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{"
                + "\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"false\"},"
                + "\"outputFormat\":\"audio-24khz-48kbitrate-mono-mp3\"}}}}\r\n";
    }

    private static String ssmlMessage(Instant now, String voiceName, String text) {
        // X-Timestamp 끝의 Z 는 Edge 가 실제로 보내는 모양을 그대로 따른 것이다.
        return "X-RequestId:" + randomId() + "\r\n"
                + "Content-Type:application/ssml+xml\r\n"
                + "X-Timestamp:" + JS_DATE.format(now) + "Z\r\n"
                + "Path:ssml\r\n\r\n"
                + ssml(voiceName, text);
    }

    static String ssml(String voiceName, String text) {
        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>"
                + "<voice name='" + voiceName + "'>"
                + "<prosody pitch='+0Hz' rate='+0%' volume='+0%'>" + escapeXml(text) + "</prosody>"
                + "</voice></speak>";
    }

    /** 사용자가 쓴 문장이 SSML 태그로 해석되지 않게 막는다. */
    static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 바이너리 프레임은 [헤더 길이 2바이트][헤더][음성] 모양이다. 음성 프레임이면 음성 부분을, 아니면 null 을 돌려준다.
     */
    static byte[] audioPayload(byte[] frame) {
        if (frame.length < 2) {
            return null;
        }
        int headerLength = ((frame[0] & 0xff) << 8) | (frame[1] & 0xff);
        if (frame.length < 2 + headerLength) {
            return null;
        }
        String header = new String(frame, 2, headerLength, StandardCharsets.UTF_8);
        if (!header.contains("Path:audio")) {
            return null;
        }
        byte[] audio = new byte[frame.length - 2 - headerLength];
        System.arraycopy(frame, 2 + headerLength, audio, 0, audio.length);
        return audio;
    }

    private static String voiceName(VoiceId voice) {
        return voice.value().substring(PREFIX.length());
    }

    private static Voice voice(String name, String label) {
        return new Voice(new VoiceId(PREFIX + name), label);
    }

    private static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 음성 프레임을 모으다가 "turn.end" 메시지가 오면 끝낸다. */
    private static final class AudioCollector implements WebSocket.Listener {

        private final ByteArrayOutputStream audio = new ByteArrayOutputStream();
        private final ByteArrayOutputStream frame = new ByteArrayOutputStream();
        private final StringBuilder text = new StringBuilder();
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();

        CompletableFuture<byte[]> result() {
            return result;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            text.append(data);
            if (last) {
                if (text.indexOf("Path:turn.end") >= 0) {
                    result.complete(audio.toByteArray());
                }
                text.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            byte[] chunk = new byte[data.remaining()];
            data.get(chunk);
            frame.writeBytes(chunk);
            if (last) {
                byte[] payload = audioPayload(frame.toByteArray());
                if (payload != null) {
                    audio.writeBytes(payload);
                }
                frame.reset();
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            result.completeExceptionally(new IllegalStateException("연결이 닫혔습니다: " + statusCode + " " + reason));
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            result.completeExceptionally(error);
        }
    }
}
