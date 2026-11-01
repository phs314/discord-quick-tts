package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.api.QuickChatRequest;
import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.api.VoiceChannelResponse;
import io.github.phs314.quicktts.bot.config.OpenApiConfig;
import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindCurrentUserVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.dto.VoiceChannelDto;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "quick chat", description = "문장 보내기와 읽힐 채널 묻기")
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
    @Operation(summary = "quick chat 보내기", description = "보낸 사람이 들어가 있는 음성 채널에서 봇이 문장을 읽는다. 읽기 시작을 기다리지 않고 202 로 답한다.")
    @ApiResponse(responseCode = "202", description = "읽을 차례에 넣음")
    @ApiResponse(responseCode = "400", description = "invalid-value (빈 문장, 너무 긴 문장, 다듬고 나면 읽을 것이 없는 문장)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
    @ApiResponse(responseCode = "401", description = "invalid-device-token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
    @ApiResponse(responseCode = "403", description = "bot-cannot-join-voice-channel",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
    @ApiResponse(responseCode = "409", description = "author-not-in-voice-channel, voice-channel-in-use",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
    @ApiResponse(responseCode = "502", description = "speech-synthesis-failed",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
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
    @Operation(summary = "읽힐 음성 채널", description = "지금 보내면 어느 음성 채널에서 읽힐지 알려 준다.")
    @ApiResponse(responseCode = "200", description = "이 채널에서 읽힘")
    @ApiResponse(responseCode = "204", description = "봇이 있는 디스코드 서버의 음성 채널에 없음", content = {})
    @ApiResponse(responseCode = "401", description = "invalid-device-token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(ref = OpenApiConfig.PROBLEM)))
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
