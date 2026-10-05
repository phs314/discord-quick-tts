package io.github.phs314.quicktts.client;

import io.github.phs314.quicktts.common.QuickChatApi;
import io.github.phs314.quicktts.common.QuickChatRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import tools.jackson.databind.json.JsonMapper;

/**
 * 봇 서버로 quick chat 문장을 보낸다.
 */
class QuickChatClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final ClientConfig config;

    QuickChatClient(ClientConfig config) {
        this.config = config;
    }

    /**
     * 문장을 보낸다. 실패하면 사용자에게 보여 줄 메시지를 담은 예외로 끝난다.
     */
    CompletableFuture<Void> send(String text) {
        String body = jsonMapper.writeValueAsString(new QuickChatRequest(config.discordUserId(), text));
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.serverUrl() + QuickChatApi.PATH))
                .header("Content-Type", "application/json")
                .header(QuickChatApi.API_KEY_HEADER, config.apiKey())
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    if (response.statusCode() / 100 != 2) {
                        throw new QuickChatException(describeFailure(response.statusCode()));
                    }
                });
    }

    private static String describeFailure(int statusCode) {
        return switch (statusCode) {
            case 400 -> "문장이 비어 있거나 너무 깁니다. (최대 " + QuickChatApi.MAX_TEXT_LENGTH + "자)";
            case 401 -> "API 키가 봇 서버와 맞지 않습니다.";
            case 409 -> "봇이 있는 서버의 음성 채널에 먼저 들어가 주세요.";
            default -> "봇 서버 오류가 났습니다. (HTTP " + statusCode + ")";
        };
    }

    static class QuickChatException extends RuntimeException {

        QuickChatException(String message) {
            super(message);
        }
    }
}
