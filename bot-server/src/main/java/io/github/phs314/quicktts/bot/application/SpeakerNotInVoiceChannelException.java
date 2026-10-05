package io.github.phs314.quicktts.bot.application;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;

/**
 * 문장을 보낸 사용자가 봇이 있는 서버의 음성 채널에 들어가 있지 않을 때 던진다.
 */
public class SpeakerNotInVoiceChannelException extends RuntimeException {

    public SpeakerNotInVoiceChannelException(DiscordUserId speaker) {
        super("사용자 " + speaker.value() + " 가 음성 채널에 없습니다.");
    }
}
