package io.github.phs314.quicktts.bot.speech.application.exception;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.ApiErrorCode;

/**
 * 봇이 같은 디스코드 서버의 다른 음성 채널에서 쓰이고 있어서 보낸 사람의 채널로 갈 수 없을 때 던진다.
 */
public class VoiceChannelInUseException extends BusinessException {

    public VoiceChannelInUseException(VoiceChannelDetails inUse) {
        super(ErrorKind.FAILED_PRECONDITION, ApiErrorCode.VOICE_CHANNEL_IN_USE,
                "봇이 이 디스코드 서버의 다른 음성 채널(" + inUse.channelName() + ")에서 쓰이고 있습니다.");
    }
}
