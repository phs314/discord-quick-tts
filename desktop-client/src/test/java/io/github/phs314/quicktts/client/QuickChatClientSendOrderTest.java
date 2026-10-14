package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 연달아 보낸 문장이 보낸 순서대로 서버에 닿는지 확인한다. 이 PC 안에 작은 가짜 서버를 띄운다.
 */
class QuickChatClientSendOrderTest {

    private final List<String> received = new CopyOnWriteArrayList<>();
    /** 첫 문장에 응답하기 시작했는지. 다음 문장이 닿았을 때 이미 true 여야 한다. */
    private final AtomicBoolean firstAnswered = new AtomicBoolean();
    private volatile boolean firstAnsweredWhenSecondArrived;
    private HttpServer server;
    private QuickChatClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            received.add(body);
            if (body.contains("second")) {
                firstAnsweredWhenSecondArrived = firstAnswered.get();
            }
            // 서버가 음성을 만드는 동안 응답이 늦는 것처럼, 첫 문장에만 시간을 끈다.
            if (body.contains("first")) {
                sleep(300);
            }
            if (body.contains("first")) {
                firstAnswered.set(true);
            }
            int status = body.contains("fail") ? 500 : 202;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        client = new QuickChatClient("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void 앞_문장의_응답을_받은_뒤에_다음_문장을_보낸다() throws Exception {
        CompletableFuture<Void> first = client.send("token", "first");
        CompletableFuture<Void> second = client.send("token", "second");

        second.get(5, TimeUnit.SECONDS);

        assertThat(first).isCompleted();
        assertThat(received).hasSize(2);
        assertThat(firstAnsweredWhenSecondArrived).isTrue();
    }

    @Test
    void 앞_문장이_실패해도_다음_문장은_보낸다() throws Exception {
        CompletableFuture<Void> failed = client.send("token", "fail");
        CompletableFuture<Void> next = client.send("token", "next");

        next.get(5, TimeUnit.SECONDS);

        assertThatThrownBy(() -> failed.get(5, TimeUnit.SECONDS)).hasCauseInstanceOf(QuickChatClient.QuickChatException.class);
        assertThat(received).hasSize(2);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
