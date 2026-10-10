package io.github.phs314.quicktts.client;

import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.DeviceRegistrationRequest;
import io.github.phs314.quicktts.common.DeviceRegistrationResponse;
import io.github.phs314.quicktts.common.QuickChatApi;
import io.github.phs314.quicktts.common.QuickChatRequest;
import io.github.phs314.quicktts.common.VoiceChannelResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 봇 서버와 이야기한다. 기기 등록과 quick chat 전송을 맡는다.
 */
class QuickChatClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final String serverUrl;

    QuickChatClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * 연결 코드로 이 PC 를 등록하고 기기 토큰을 받는다. 디스코드 {@code /연결해제} 목록에는 컴퓨터 이름으로 보인다.
     *
     * @throws QuickChatException 코드가 틀렸거나 서버에 닿지 못했을 때
     */
    String register(String pairingCode) {
        HttpRequest request = jsonPost(QuickChatApi.DEVICES_PATH,
                new DeviceRegistrationRequest(pairingCode, computerName())).build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                return jsonMapper.readValue(response.body(), DeviceRegistrationResponse.class).deviceToken();
            }
            throw registerFailure(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new QuickChatException("봇 서버에 연결하지 못했습니다: " + serverUrl);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QuickChatException("등록이 중단되었습니다.");
        }
    }

    /**
     * 문장을 보낸다. 실패하면 사용자에게 보여 줄 메시지를 담은 예외로 끝난다.
     * 기기 토큰이 더 이상 유효하지 않으면 {@link DeviceUnauthorizedException} 으로 끝난다.
     */
    CompletableFuture<Void> send(String deviceToken, String text) {
        HttpRequest request = jsonPost(QuickChatApi.PATH, new QuickChatRequest(text))
                .header("Authorization", QuickChatApi.BEARER_PREFIX + deviceToken)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 202) {
                        throw sendFailure(response.statusCode(), response.body());
                    }
                });
    }

    /** 연결 코드 등록이 실패한 응답을 사용자에게 보여 줄 예외로 바꾼다. */
    QuickChatException registerFailure(int status, String body) {
        ServerError error = serverError(body);
        return switch (error.code()) {
            case ApiErrorCode.INVALID_PAIRING_CODE, ApiErrorCode.INVALID_VALUE ->
                    new QuickChatException("연결 코드가 틀렸거나 만료되었습니다. 디스코드에서 /연결 을 다시 입력해 주세요.");
            default -> unexpected(status);
        };
    }

    /** 문장 전송이 실패한 응답을 사용자에게 보여 줄 예외로 바꾼다. 안내 문구는 응답의 오류 코드로 고른다. */
    QuickChatException sendFailure(int status, String body) {
        ServerError error = serverError(body);
        return switch (error.code()) {
            case ApiErrorCode.DEVICE_UNAUTHORIZED -> new DeviceUnauthorizedException();
            case ApiErrorCode.INVALID_VALUE -> new QuickChatException(error.detailOr(
                    "문장이 비어 있거나 너무 깁니다. (최대 " + QuickChatApi.MAX_TEXT_LENGTH + "자)"));
            case ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL ->
                    new QuickChatException("봇이 있는 서버의 음성 채널에 먼저 들어가 주세요.");
            case ApiErrorCode.VOICE_CHANNEL_IN_USE ->
                    new QuickChatException("봇이 이 서버의 다른 음성 채널에서 쓰이고 있습니다. 그 채널이 비면 쓸 수 있습니다.");
            case ApiErrorCode.SPEECH_SYNTHESIS_FAILED ->
                    new QuickChatException("음성을 만들지 못했습니다. 잠시 뒤 다시 보내 주세요.");
            default -> unexpected(status);
        };
    }

    private static QuickChatException unexpected(int status) {
        return new QuickChatException("봇 서버 오류가 났습니다. (HTTP " + status + ")");
    }

    /** 오류 응답 본문에서 오류 코드와 설명을 꺼낸다. 본문이 오류 응답 모양이 아니면 둘 다 비어 있다. */
    private ServerError serverError(String body) {
        try {
            JsonNode problem = jsonMapper.readTree(body == null ? "" : body);
            return new ServerError(text(problem, ApiErrorCode.PROPERTY), text(problem, "detail"));
        } catch (JacksonException e) {
            return new ServerError("", "");
        }
    }

    private static String text(JsonNode node, String property) {
        JsonNode value = node == null ? null : node.get(property);
        return value != null && value.isString() ? value.asString() : "";
    }

    private record ServerError(String code, String detail) {

        String detailOr(String fallback) {
            return detail.isBlank() ? fallback : detail;
        }
    }

    private static String computerName() {
        String name = System.getenv("COMPUTERNAME");
        if (name == null || name.isBlank()) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                name = "";
            }
        }
        return name;
    }

    /**
     * 지금 보내면 어느 음성 채널에서 읽힐지 묻는다. 음성 채널에 없으면 빈 값으로 끝난다.
     */
    CompletableFuture<Optional<VoiceChannelResponse>> myVoiceChannel(String deviceToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(serverUrl + QuickChatApi.MY_VOICE_CHANNEL_PATH))
                .header("Authorization", QuickChatApi.BEARER_PREFIX + deviceToken)
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> switch (response.statusCode()) {
                    case 200 -> Optional.of(jsonMapper.readValue(response.body(), VoiceChannelResponse.class));
                    case 204 -> Optional.<VoiceChannelResponse>empty();
                    default -> throw new QuickChatException("음성 채널을 확인하지 못했습니다. (HTTP " + response.statusCode() + ")");
                });
    }

    private HttpRequest.Builder jsonPost(String path, Object body) {
        return HttpRequest.newBuilder(URI.create(serverUrl + path))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body)));
    }

    static class QuickChatException extends RuntimeException {

        QuickChatException(String message) {
            super(message);
        }
    }

    static class DeviceUnauthorizedException extends QuickChatException {

        DeviceUnauthorizedException() {
            super("이 PC 의 연결이 해제되었습니다. 디스코드에서 /연결 로 다시 연결해 주세요.");
        }
    }
}
