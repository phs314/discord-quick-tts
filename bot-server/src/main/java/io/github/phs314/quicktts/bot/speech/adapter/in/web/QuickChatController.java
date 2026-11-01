package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindCurrentUserVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.QuickChatRequest;
import io.github.phs314.quicktts.common.QuickTtsApi;
import io.github.phs314.quicktts.common.VoiceChannelResponse;
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
 */
@RestController
@RequiredArgsConstructor
public class QuickChatController {

    private final SpeakQuickChatUseCase speakQuickChatUseCase;
    private final FindCurrentUserVoiceChannelUseCase findCurrentUserVoiceChannelUseCase;

    @PostMapping(QuickTtsApi.QUICK_CHAT_PATH)
    public ResponseEntity<Void> quickChat(
            @AuthenticationPrincipal DiscordUserId author,
            @RequestBody QuickChatRequest request) {
        SpeakQuickChatCommand command = QuickChatWebMapper.toCommand(author, request);
        speakQuickChatUseCase.speak(command);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(QuickTtsApi.MY_VOICE_CHANNEL_PATH)
    public ResponseEntity<VoiceChannelResponse> myVoiceChannel(@AuthenticationPrincipal DiscordUserId user) {
        FindCurrentUserVoiceChannelQuery query = QuickChatWebMapper.toQuery(user);
        Optional<VoiceChannelDetails> details = findCurrentUserVoiceChannelUseCase.findCurrentUserVoiceChannel(query);
        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        VoiceChannelResponse response = QuickChatWebMapper.toResponse(details.get());
        return ResponseEntity.ok(response);
    }
}
