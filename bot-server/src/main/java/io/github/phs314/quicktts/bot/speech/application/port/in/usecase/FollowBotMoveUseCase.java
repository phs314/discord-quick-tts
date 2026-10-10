package io.github.phs314.quicktts.bot.speech.application.port.in.usecase;

import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;

/**
 * 디스코드에서 봇이 옮겨지거나 끊긴 것을 봇 자리에 반영한다. 관리자가 봇을 다른 채널로 끌어 옮기거나 연결을 끊은 경우다.
 */
public interface FollowBotMoveUseCase {

    /**
     * @param from 봇이 있던 음성 채널
     * @param to   봇이 옮겨진 음성 채널. 끊겼으면 빈 값
     */
    void followBotMove(VoiceChannel from, Optional<VoiceChannel> to);
}
