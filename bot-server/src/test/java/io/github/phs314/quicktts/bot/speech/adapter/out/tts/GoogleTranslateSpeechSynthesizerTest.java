package io.github.phs314.quicktts.bot.speech.adapter.out.tts;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class GoogleTranslateSpeechSynthesizerTest {

    @Test
    void 한글_문장을_URL_인코딩해서_요청_주소를_만든다() {
        URI uri = GoogleTranslateSpeechSynthesizer.buildUri("안녕 하세요", "ko");

        assertThat(uri.getRawQuery())
                .contains("tl=ko")
                .contains("q=%EC%95%88%EB%85%95+%ED%95%98%EC%84%B8%EC%9A%94");
    }
}
