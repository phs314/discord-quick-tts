package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 봇이 들어가고 읽고 나간 순서를 글로 남긴다. 예: {@code join:2}, {@code play:edge:sunhi:안녕}, {@code leave}.
 */
class RecordingSpeechPlayer implements SpeechPlayerPort {

    final List<String> actions = new ArrayList<>();

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
        actions.add("leave");
    }
}
