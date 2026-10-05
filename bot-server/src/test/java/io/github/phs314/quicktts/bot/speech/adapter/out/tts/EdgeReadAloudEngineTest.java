package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EdgeReadAloudEngineTest {

    @Test
    void 접속_토큰은_5분_단위로_같은_값이다() {
        String expected = "1EA3E2BC9E57A1066B9FAAB704FDB409C014D26147456351113CC33834B468CD";

        assertThat(EdgeReadAloudEngine.secMsGec(Instant.parse("2026-10-05T00:02:30Z"))).isEqualTo(expected);
        assertThat(EdgeReadAloudEngine.secMsGec(Instant.parse("2026-10-05T00:00:00Z"))).isEqualTo(expected);
        assertThat(EdgeReadAloudEngine.secMsGec(Instant.parse("2026-10-05T00:05:00Z"))).isNotEqualTo(expected);
    }

    @Test
    void 사용자_문장은_SSML_태그로_해석되지_않게_바꾼다() {
        String ssml = EdgeReadAloudEngine.ssml("ko-KR-SunHiNeural", "<break time='5s'/> A&B");

        assertThat(ssml)
                .contains("<voice name='ko-KR-SunHiNeural'>")
                .contains("&lt;break time=&apos;5s&apos;/&gt; A&amp;B")
                .doesNotContain("<break");
    }

    @Test
    void 음성_프레임에서는_헤더_뒤의_음성만_꺼낸다() {
        byte[] audio = {1, 2, 3};

        assertThat(EdgeReadAloudEngine.audioPayload(frame("X-RequestId:1\r\nPath:audio\r\n", audio))).containsExactly(1, 2, 3);
        assertThat(EdgeReadAloudEngine.audioPayload(frame("Path:turn.start\r\n", audio))).isNull();
        assertThat(EdgeReadAloudEngine.audioPayload(new byte[] {0})).isNull();
    }

    private static byte[] frame(String header, byte[] audio) {
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(headerBytes.length >> 8);
        frame.write(headerBytes.length & 0xff);
        frame.writeBytes(headerBytes);
        frame.writeBytes(audio);
        return frame.toByteArray();
    }
}
