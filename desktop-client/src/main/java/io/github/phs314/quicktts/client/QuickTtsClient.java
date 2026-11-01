package io.github.phs314.quicktts.client;

import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.DeviceRegistrationRequest;
import io.github.phs314.quicktts.common.DeviceRegistrationResponse;
import io.github.phs314.quicktts.common.QuickChatRequest;
import io.github.phs314.quicktts.common.QuickTtsApi;
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
 * 봇 서버 API 를 부른다. 기기 등록, quick chat 전송, 지금 음성 채널 확인을 맡는다.
 */
class QuickTtsClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final String serverUrl;
    /** 바로 앞에 보낸 문장. 다음 문장은 이게 끝난 뒤에 보낸다. */
    private CompletableFuture<Void> previousSend = CompletableFuture.completedFuture(null);

    QuickTtsClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * 연결 코드로 이 PC 를 등록하고 기기 토큰을 받는다. 디스코드 {@code /연결해제} 목록에는 컴퓨터 이름으로 보인다.
     *
     * @throws QuickTtsClientException 코드가 틀렸거나 서버에 닿지 못했을 때
     */
    String registerDevice(String pairingCode) {
        HttpRequest request = jsonPost(QuickTtsApi.DEVICES_PATH,
                new DeviceRegistrationRequest(pairingCode, computerName())).build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                return jsonMapper.readValue(response.body(), DeviceRegistrationResponse.class).deviceToken();
            }
            throw registerFailure(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new QuickTtsClientException("봇 서버에 연결하지 못했습니다: " + serverUrl);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QuickTtsClientException("등록이 중단되었습니다.");
        }
    }

    /**
     * 문장을 보낸다. 실패하면 사용자에게 보여 줄 메시지를 담은 예외로 끝난다.
     * 기기 토큰이 더 이상 유효하지 않으면 {@link DeviceUnauthorizedException} 으로 끝난다.
     * <p>서버는 음성을 다 만든 문장부터 읽으므로, 앞 문장의 응답을 받은 뒤에 보내야 연달아 보내도 순서가 바뀌지 않는다.
     */
    synchronized CompletableFuture<Void> send(String deviceToken, String text) {
        HttpRequest request = jsonPost(QuickTtsApi.QUICK_CHAT_PATH, new QuickChatRequest(text))
                .header("Authorization", QuickTtsApi.BEARER_PREFIX + deviceToken)
                .build();

        CompletableFuture<Void> sent = previousSend
                .handle((ignored, previousFailure) -> null)
                .thenCompose(ignored -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                .thenAccept(response -> {
                    if (response.statusCode() != 202) {
                        throw sendFailure(response.statusCode(), response.body());
                    }
                });
        previousSend = sent;
        return sent;
    }

    /** 연결 코드 등록이 실패한 응답을 사용자에게 보여 줄 예외로 바꾼다. */
    QuickTtsClientException registerFailure(int status, String body) {
        ServerError error = serverError(body);
        return switch (error.code()) {
            case ApiErrorCode.INVALID_PAIRING_CODE, ApiErrorCode.INVALID_VALUE ->
                    new QuickTtsClientException("연결 코드가 틀렸거나 만료되었습니다. 디스코드에서 /연결 을 다시 입력해 주세요.");
            default -> unexpectedFailure(status);
        };
    }

    /** 문장 전송이 실패한 응답을 사용자에게 보여 줄 예외로 바꾼다. 안내 문구는 응답의 오류 코드로 고른다. */
    QuickTtsClientException sendFailure(int status, String body) {
        ServerError error = serverError(body);
        return switch (error.code()) {
            case ApiErrorCode.DEVICE_UNAUTHORIZED -> new DeviceUnauthorizedException();
            case ApiErrorCode.INVALID_VALUE -> new QuickTtsClientException(error.detailOr(
                    "문장이 비어 있거나 너무 깁니다. (최대 " + QuickTtsApi.MAX_TEXT_LENGTH + "자)"));
            case ApiErrorCode.AUTHOR_NOT_IN_VOICE_CHANNEL ->
                    new QuickTtsClientException("봇이 있는 서버의 음성 채널에 먼저 들어가 주세요.");
            case ApiErrorCode.VOICE_CHANNEL_IN_USE ->
                    new QuickTtsClientException("봇이 이 서버의 다른 음성 채널에서 쓰이고 있습니다. 그 채널이 비면 쓸 수 있습니다.");
            case ApiErrorCode.BOT_CANNOT_JOIN_VOICE_CHANNEL ->
                    new QuickTtsClientException("봇이 이 음성 채널에 들어갈 수 없습니다. 채널 인원이 꽉 찼거나 봇에게 연결 권한이 없습니다.");
            case ApiErrorCode.SPEECH_SYNTHESIS_FAILED ->
                    new QuickTtsClientException("음성을 만들지 못했습니다. 잠시 뒤 다시 보내 주세요.");
            default -> unexpectedFailure(status);
        };
    }

    /** 내 음성 채널 묻기가 실패한 응답을 예외로 바꾼다. 기기 연결이 끊겼으면 다시 연결하도록 따로 알린다. */
    QuickTtsClientException voiceChannelFailure(int status, String body) {
        if (ApiErrorCode.DEVICE_UNAUTHORIZED.equals(serverError(body).code())) {
            return new DeviceUnauthorizedException();
        }
        return new QuickTtsClientException("음성 채널을 확인하지 못했습니다. (HTTP " + status + ")");
    }

    private static QuickTtsClientException unexpectedFailure(int status) {
        return new QuickTtsClientException("봇 서버 오류가 났습니다. (HTTP " + status + ")");
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
    CompletableFuture<Optional<VoiceChannelResponse>> findMyVoiceChannel(String deviceToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(serverUrl + QuickTtsApi.MY_VOICE_CHANNEL_PATH))
                .header("Authorization", QuickTtsApi.BEARER_PREFIX + deviceToken)
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> switch (response.statusCode()) {
                    case 200 -> Optional.of(jsonMapper.readValue(response.body(), VoiceChannelResponse.class));
                    case 204 -> Optional.<VoiceChannelResponse>empty();
                    default -> throw voiceChannelFailure(response.statusCode(), response.body());
                });
    }

    private HttpRequest.Builder jsonPost(String path, Object body) {
        return HttpRequest.newBuilder(URI.create(serverUrl + path))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body)));
    }

    static class QuickTtsClientException extends RuntimeException {

        QuickTtsClientException(String message) {
            super(message);
        }
    }

    static class DeviceUnauthorizedException extends QuickTtsClientException {

        DeviceUnauthorizedException() {
            super("이 PC 의 연결이 해제되었습니다. 디스코드에서 /연결 로 다시 연결해 주세요.");
        }
    }
}
