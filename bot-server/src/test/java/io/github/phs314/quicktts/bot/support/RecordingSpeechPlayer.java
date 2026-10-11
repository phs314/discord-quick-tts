package io.github.phs314.quicktts.bot.support;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * API 통합 테스트에서 디스코드 음성 연결 대신 쓰는 대역. 소리는 내지 않고 봇이 들어가고 읽고 나간 순서만 글로 남긴다.
 * 예: {@code join:2}, {@code play:fake:sunhi:안녕}, {@code leave:1}.
 */
public class RecordingSpeechPlayer implements SpeechPlayerPort {

    private final List<String> actions = new CopyOnWriteArrayList<>();

    public List<String> actions() {
        return List.copyOf(actions);
    }

    public void reset() {
        actions.clear();
    }

    @Override
    public void join(VoiceChannel channel) {
        actions.add("join:" + channel.channelId());
    }

    @Override
    public void play(GuildId guildId, Speech speech) {
        actions.add("play:" + new String(speech.audio(), StandardCharsets.UTF_8));
    }

    @Override
    public void leave(GuildId guildId) {
        actions.add("leave:" + guildId.value());
    }
}
