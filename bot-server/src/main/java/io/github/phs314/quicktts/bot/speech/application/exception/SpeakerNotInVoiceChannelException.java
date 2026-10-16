package io.github.phs314.quicktts.bot.speech.application.exception;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.common.ApiErrorCode;

/**
 * 문장을 보낸 사용자가 봇이 있는 서버의 음성 채널에 들어가 있지 않을 때 던진다.
 */
public class SpeakerNotInVoiceChannelException extends BusinessException {

    public SpeakerNotInVoiceChannelException(DiscordUserId speaker) {
        super(ErrorKind.CONFLICT, ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL, "사용자 " + speaker.value() + " 가 음성 채널에 없습니다.");
    }
}
