package io.github.phs314.quicktts.bot.speech.application.exception;

import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;

/**
 * 봇이 그 음성 채널에 들어갈 수 없을 때 던진다. 채널 인원이 꽉 찼거나 봇에게 연결 권한이 없는 경우다.
 * 음성 연결 어댑터가 던지고 유스케이스를 거쳐 웹 어댑터까지 올라간다.
 */
public class BotCannotJoinVoiceChannelException extends RuntimeException {

    public BotCannotJoinVoiceChannelException(VoiceChannel channel, Throwable cause) {
        super("봇이 이 음성 채널에 들어갈 수 없습니다. 채널 인원이 꽉 찼거나 봇에게 연결 권한이 없습니다: " + channel, cause);
    }
}
