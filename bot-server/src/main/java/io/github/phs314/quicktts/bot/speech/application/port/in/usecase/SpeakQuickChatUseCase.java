package io.github.phs314.quicktts.bot.speech.application.port.in.usecase;

import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;

/**
 * quick chat 문장을 보낸 사용자가 있는 음성 채널에서 읽어 준다.
 */
public interface SpeakQuickChatUseCase {

    /**
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException
     *         보낸 사용자가 봇이 있는 서버의 음성 채널에 없을 때
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException
     *         보낸 사용자의 음성 채널에 봇이 들어갈 수 없을 때 (인원이 꽉 참, 연결 권한 없음)
     * @throws io.github.phs314.quicktts.bot.speech.application.exception.SpeechSynthesisException
     *         음성 생성에 실패했을 때
     */
    void speak(SpeakQuickChatCommand command);
}
