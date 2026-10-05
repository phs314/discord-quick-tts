package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.domain.QuickChatMessage;
import io.github.phs314.quicktts.common.QuickChatApi;
import io.github.phs314.quicktts.common.QuickChatRequest;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데스크톱 클라이언트가 보내는 quick chat 요청을 받는 인바운드 어댑터.
 * 보낸 사람은 device 컨텍스트의 공개 유스케이스에 기기 토큰을 물어서 알아낸다.
 */
@RestController
public class QuickChatController {

    private final AuthenticateDeviceUseCase authenticateDevice;
    private final SpeakQuickChatUseCase speakQuickChat;

    public QuickChatController(AuthenticateDeviceUseCase authenticateDevice, SpeakQuickChatUseCase speakQuickChat) {
        this.authenticateDevice = authenticateDevice;
        this.speakQuickChat = speakQuickChat;
    }

    @PostMapping(QuickChatApi.PATH)
    public ResponseEntity<Void> quickChat(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody QuickChatRequest request) {
        Optional<DiscordUserId> speaker = bearerToken(authorization).flatMap(authenticateDevice::authenticate);
        if (speaker.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        speakQuickChat.speak(new SpeakQuickChatCommand(speaker.get(), new QuickChatMessage(request.text())));
        return ResponseEntity.accepted().build();
    }

    private static Optional<String> bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(QuickChatApi.BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(QuickChatApi.BEARER_PREFIX.length()).strip();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
