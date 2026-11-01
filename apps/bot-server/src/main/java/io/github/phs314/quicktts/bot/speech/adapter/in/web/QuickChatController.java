package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.api.QuickChatRequest;
import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.api.VoiceChannelResponse;
import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindCurrentUserVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데스크톱 클라이언트가 보내는 quick chat 요청과 "어느 채널에서 읽힐지" 조회를 받는 인바운드 어댑터.
 * 보낸 사람은 기기 토큰 인증(ADR 0031)이 알아낸 디스코드 사용자로 받는다.
 * quick chat 을 받고 재생 순서에 넣기까지 걸린 시간을 {@value #SPEAK_METRIC} 지표로 잰다 (ADR 0053).
 */
@RestController
@RequiredArgsConstructor
public class QuickChatController {

    static final String SPEAK_METRIC = "quicktts.quickchat.speak";
    /** 실패하지 않았을 때의 {@code error} 태그 값. */
    static final String NO_ERROR = "none";
    /** {@link BusinessException} 이 아닌, 예상하지 못한 실패의 {@code error} 태그 값. */
    static final String UNEXPECTED_ERROR = "unexpected";

    private final SpeakQuickChatUseCase speakQuickChatUseCase;
    private final FindCurrentUserVoiceChannelUseCase findCurrentUserVoiceChannelUseCase;
    private final MeterRegistry meterRegistry;

    @PostMapping(QuickTtsApi.QUICK_CHAT_PATH)
    public ResponseEntity<Void> quickChat(
            @AuthenticationPrincipal DiscordUserId author,
            @RequestBody QuickChatRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String error = UNEXPECTED_ERROR;
        try {
            SpeakQuickChatCommand command = QuickChatWebMapper.toCommand(author, request);
            speakQuickChatUseCase.speak(command);
            error = NO_ERROR;
            return ResponseEntity.accepted().build();
        } catch (BusinessException e) {
            error = e.code();
            throw e;
        } finally {
            stopSpeakTimer(sample, error);
        }
    }

    @GetMapping(QuickTtsApi.MY_VOICE_CHANNEL_PATH)
    public ResponseEntity<VoiceChannelResponse> myVoiceChannel(@AuthenticationPrincipal DiscordUserId user) {
        FindCurrentUserVoiceChannelQuery query = QuickChatWebMapper.toQuery(user);
        Optional<VoiceChannelDto> details = findCurrentUserVoiceChannelUseCase.findCurrentUserVoiceChannel(query);
        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        VoiceChannelResponse response = QuickChatWebMapper.toResponse(details.get());
        return ResponseEntity.ok(response);
    }

    /**
     * 결과(success, failure)와 오류 코드({@code ApiErrorCode} 값, 성공이면 none)별로 센다.
     * "빠름" 은 success 만 보면 되고, 실패가 무엇 때문인지는 error 로 본다.
     */
    private void stopSpeakTimer(Timer.Sample sample, String error) {
        String outcome = NO_ERROR.equals(error) ? "success" : "failure";
        sample.stop(meterRegistry.timer(SPEAK_METRIC, "outcome", outcome, "error", error));
    }
}
