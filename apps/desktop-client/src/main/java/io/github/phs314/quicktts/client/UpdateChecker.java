package io.github.phs314.quicktts.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * GitHub Releases 에 지금보다 새 버전의 QuickTTS 가 올라왔는지 확인한다. 내려받기와 설치는 사용자가 직접 한다.
 */
final class UpdateChecker {

    /** 초안과 미리 보기(prerelease)를 뺀 가장 최근 Release. */
    private static final URI LATEST_RELEASE = URI.create(
            "https://api.github.com/repos/phs314/discord-quick-tts/releases/latest");
    private static final Pattern VERSION = Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final String currentVersion;

    private UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    /**
     * exe 로 실행했으면 그 버전으로 확인한다. exe 를 만들 때 {@code -Dquicktts.version=...} 이 들어간다.
     * Gradle 로 바로 띄운 개발 중에는 버전을 모르므로 확인하지 않는다.
     */
    static Optional<UpdateChecker> forThisApp() {
        return Optional.ofNullable(System.getProperty("quicktts.version"))
                .filter(version -> VERSION.matcher(version).matches())
                .map(UpdateChecker::new);
    }

    /** 새 버전이 있으면 그 Release 로 끝난다. 확인하지 못했거나 이미 최신이면 빈 값으로 끝난다. */
    CompletableFuture<Optional<Release>> newerRelease() {
        HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "QuickTTS/" + currentVersion)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200
                        ? newerRelease(currentVersion, response.body())
                        : Optional.<Release>empty())
                .exceptionally(failure -> Optional.empty());
    }

    /** GitHub 의 최신 Release 응답에서 태그와 Release 페이지를 꺼내, 지금 버전보다 새것일 때만 돌려준다. */
    static Optional<Release> newerRelease(String current, String body) {
        try {
            JsonNode release = JSON.readTree(body == null ? "" : body);
            String tag = text(release, "tag_name");
            String page = text(release, "html_url");
            if (page.isEmpty() || !isNewer(tag, current)) {
                return Optional.empty();
            }
            return Optional.of(new Release(tag.startsWith("v") ? tag.substring(1) : tag, URI.create(page)));
        } catch (JacksonException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** {@code v1.2.3} 이나 {@code 1.2.3} 모양만 비교한다. 모양이 다르면 새것으로 보지 않는다. */
    static boolean isNewer(String candidate, String current) {
        Matcher a = VERSION.matcher(candidate);
        Matcher b = VERSION.matcher(current);
        if (!a.matches() || !b.matches()) {
            return false;
        }
        for (int group = 1; group <= 3; group++) {
            int compared = Integer.compare(Integer.parseInt(a.group(group)), Integer.parseInt(b.group(group)));
            if (compared != 0) {
                return compared > 0;
            }
        }
        return false;
    }

    private static String text(JsonNode node, String property) {
        JsonNode value = node == null ? null : node.get(property);
        return value != null && value.isString() ? value.asString() : "";
    }

    /** 새로 나온 버전과 설치 파일을 받을 수 있는 Release 페이지. */
    record Release(String version, URI page) {
    }
}
