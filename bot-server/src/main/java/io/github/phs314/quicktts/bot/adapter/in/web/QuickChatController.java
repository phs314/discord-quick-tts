package io.github.phs314.quicktts.bot.adapter.in.web;

import io.github.phs314.quicktts.bot.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.config.QuickTtsProperties;
import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.QuickChatMessage;
import io.github.phs314.quicktts.common.QuickChatApi;
import io.github.phs314.quicktts.common.QuickChatRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데스크톱 클라이언트가 보내는 quick chat 요청을 받는 인바운드 어댑터.
 */
@RestController
public class QuickChatController {

    private final SpeakQuickChatUseCase speakQuickChat;
    private final byte[] apiKey;

    public QuickChatController(SpeakQuickChatUseCase speakQuickChat, QuickTtsProperties properties) {
        this.speakQuickChat = speakQuickChat;
        this.apiKey = properties.apiKey().getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping(QuickChatApi.PATH)
    public ResponseEntity<Void> quickChat(
            @RequestHeader(name = QuickChatApi.API_KEY_HEADER, required = false) String requestApiKey,
            @RequestBody QuickChatRequest request) {
        if (!isValidApiKey(requestApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        speakQuickChat.speak(new SpeakQuickChatCommand(
                DiscordUserId.parse(request.discordUserId()),
                new QuickChatMessage(request.text())));
        return ResponseEntity.accepted().build();
    }

    private boolean isValidApiKey(String requestApiKey) {
        return requestApiKey != null
                && MessageDigest.isEqual(apiKey, requestApiKey.getBytes(StandardCharsets.UTF_8));
    }
}
