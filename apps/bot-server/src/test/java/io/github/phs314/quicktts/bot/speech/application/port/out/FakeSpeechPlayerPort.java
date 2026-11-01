package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.ChannelId;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 디스코드 음성 연결 대신 쓰는 대역. 소리는 내지 않고 봇이 들어가고 읽고 나간 순서만 글로 남긴다.
 * 예: {@code join:2}, {@code play:fake:sunhi:안녕}, {@code leave:1}, {@code forget:1}. 통합 테스트에서는 웹 서버 스레드가 쓰므로 여러 스레드에서 써도 된다.
 */
public class FakeSpeechPlayerPort implements SpeechPlayerPort {

    private final List<String> actions = new CopyOnWriteArrayList<>();

    public List<String> actions() {
        return List.copyOf(actions);
    }

    public void reset() {
        actions.clear();
    }

    @Override
    public void join(VoiceChannel channel) {
        actions.add("join:" + channel.channelId().value());
    }

    @Override
    public void play(GuildId guildId, Audio audio) {
        actions.add("play:" + new String(audio.content(), StandardCharsets.UTF_8));
    }

    @Override
    public void leave(GuildId guildId) {
        actions.add("leave:" + guildId.value());
    }

    @Override
    public void forget(GuildId guildId) {
        actions.add("forget:" + guildId.value());
    }
}
