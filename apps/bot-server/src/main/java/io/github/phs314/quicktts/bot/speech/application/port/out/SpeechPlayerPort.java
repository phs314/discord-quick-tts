package io.github.phs314.quicktts.bot.speech.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;

/**
 * 디스코드 음성 채널에 봇을 들이고 내보내며 음성을 재생한다. 어느 채널로 갈지는 부르는 쪽이 정한다.
 */
public interface SpeechPlayerPort {

    /**
     * 봇을 그 음성 채널로 들인다. 같은 디스코드 서버의 다른 채널에 있었으면 옮겨 간다. 연결은 조금 뒤에 끝난다.
     *
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException
     *         채널 인원이 꽉 찼거나 봇에게 연결 권한이 없을 때
     */
    void join(VoiceChannel channel);

    /**
     * 봇이 그 디스코드 서버에서 들어가 있는 채널에서 음성을 재생한다. 앞 음성이 끝난 뒤 다음 음성을 재생한다.
     *
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.UnplayableAudioException
     *         음성이 재생할 수 없는 형식일 때
     */
    void play(GuildId guildId, Audio audio);

    /** 읽던 문장과 기다리던 문장을 모두 버리고 음성 채널에서 나간다. */
    void leave(GuildId guildId);

    /**
     * 봇이 그 디스코드 서버에서 나간 뒤에 부른다. 읽던 문장과 기다리던 문장, 그 서버의 플레이어를 모두 버린다.
     * 디스코드 음성 연결은 이미 끊겼으므로 건드리지 않는다.
     */
    void forget(GuildId guildId);
}
