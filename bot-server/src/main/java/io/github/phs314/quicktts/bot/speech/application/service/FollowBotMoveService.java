package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.FollowBotMoveUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowBotMoveService implements FollowBotMoveUseCase {

    private final BotSeatPort botSeats;
    private final SpeechPlayerPort speechPlayer;

    @Override
    public void followBotMove(VoiceChannel from, Optional<VoiceChannel> to) {
        botSeats.withSeat(from.guildId(), seat -> {
            // 끊겼으면 남은 문장이 다음에 들어가는 채널에서 읽히지 않게 버린다.
            if (seat.followBotMove(from, to)) {
                speechPlayer.leave(from.guildId());
            }
        });
    }
}
