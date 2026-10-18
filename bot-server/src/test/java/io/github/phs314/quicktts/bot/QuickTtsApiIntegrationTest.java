package io.github.phs314.quicktts.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.github.phs314.quicktts.bot.config.DiscordCommandRegistrar;
import io.github.phs314.quicktts.bot.device.application.port.in.command.IssuePairingCodeCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.usecase.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.ApiErrorCode;
import io.github.phs314.quicktts.common.DeviceRegistrationRequest;
import io.github.phs314.quicktts.common.DeviceRegistrationResponse;
import io.github.phs314.quicktts.common.QuickChatRequest;
import io.github.phs314.quicktts.common.QuickTtsApi;
import io.github.phs314.quicktts.common.VoiceChannelResponse;
import java.util.concurrent.atomic.AtomicLong;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * 봇 서버를 통째로 띄우고 데스크톱 클라이언트처럼 HTTP 로 /api/v1 을 불러 응답까지 확인한다.
 * 컨트롤러부터 서비스, 도메인, DB(Flyway 마이그레이션 포함)까지는 실제 것을 쓴다.
 * 디스코드와 TTS 엔진은 출력 포트 대역으로 바꿔서 디스코드에 접속하지 않고 소리도 나지 않는다.
 * 디스코드 /연결, /연결해제, /목소리 명령이 할 일은 그 명령이 부르는 입력 포트를 직접 부른다.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "quicktts.discord.token=test-token",
        // PC 서버와 같은 H2(MySQL 모드)를 파일 대신 메모리에 띄운다. MySQL 쪽 SQL 은 저장소 테스트가 확인한다.
        "spring.datasource.url=jdbc:h2:mem:api-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
// 진짜 JDA 를 만들면 디스코드에 로그인하므로 빈 껍데기로 바꾸고, 켜질 때 디스코드를 부르는 빈도 함께 바꾼다.
@MockitoBean(types = {JDA.class, DiscordCommandRegistrar.class, BotOwnerPort.class, JoinedGuildPort.class})
class QuickTtsApiIntegrationTest {

    private static final FakeVoiceChannelLocatorPort VOICE_CHANNELS = new FakeVoiceChannelLocatorPort();
    private static final FakeSpeechPlayerPort PLAYER = new FakeSpeechPlayerPort();
    private static final FakeSpeechSynthesizerPort TTS = new FakeSpeechSynthesizerPort();

    /** 테스트끼리 봇 자리, 목소리 설정, 기기가 섞이지 않게 사용자, 디스코드 서버, 채널마다 새 ID 를 쓴다. */
    private static final AtomicLong IDS = new AtomicLong(1_000);

    @TestBean(methodName = "voiceChannels")
    VoiceChannelLocatorPort voiceChannelLocatorPort;

    @TestBean(methodName = "player")
    SpeechPlayerPort speechPlayerPort;

    @TestBean(methodName = "tts")
    SpeechSynthesizerPort speechSynthesizerPort;

    @LocalServerPort
    int port;

    @Autowired
    IssuePairingCodeUseCase issuePairingCodeUseCase;

    @Autowired
    ManageOwnDevicesUseCase manageOwnDevicesUseCase;

    @Autowired
    ManageVoiceUseCase manageVoiceUseCase;

    RestTestClient client;

    static VoiceChannelLocatorPort voiceChannels() {
        return VOICE_CHANNELS;
    }

    static SpeechPlayerPort player() {
        return PLAYER;
    }

    static SpeechSynthesizerPort tts() {
        return TTS;
    }

    @BeforeEach
    void setUp() {
        VOICE_CHANNELS.reset();
        PLAYER.reset();
        TTS.reset();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void 연결_코드로_PC_를_등록하면_201_과_기기_토큰을_주고_그_토큰으로_API_를_쓸_수_있다() {
        String token = registerDevice(newUser());

        client.get().uri(QuickTtsApi.MY_VOICE_CHANNEL_PATH)
                .header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void 한_번_쓴_연결_코드는_400_과_invalid_pairing_code() {
        String code = issuePairingCodeUseCase.issue(new IssuePairingCodeCommand(newUser())).code().display();
        postDevice(code).expectStatus().isCreated();

        postDevice(code)
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.INVALID_PAIRING_CODE);
    }

    @Test
    void 연결_코드_모양이_틀리면_400_과_invalid_value() {
        postDevice("ABC")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.INVALID_VALUE);
    }

    @Test
    void 내_음성_채널을_물으면_디스코드_서버와_채널_이름을_준다() {
        DiscordUserId user = newUser();
        String token = registerDevice(user);
        VOICE_CHANNELS.enter(user, details(newChannel(newGuild()), "게임"));

        VoiceChannelResponse response = client.get().uri(QuickTtsApi.MY_VOICE_CHANNEL_PATH)
                .header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VoiceChannelResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isEqualTo(new VoiceChannelResponse("우리 서버", "https://example.com/icon.png", "게임"));
    }

    @Test
    void 보낸_문장은_보낸_사람의_음성_채널에서_고른_목소리로_읽는다() {
        DiscordUserId user = newUser();
        String token = registerDevice(user);
        VoiceChannel channel = newChannel(newGuild());
        VOICE_CHANNELS.enter(user, details(channel, "게임"));
        manageVoiceUseCase.changeVoice(new ChangeVoiceCommand(user, FakeSpeechSynthesizerPort.INJOON.id()));

        sendQuickChat(token, "안녕").expectStatus().isAccepted();

        assertThat(PLAYER.actions()).containsExactly("join:" + channel.channelId(), "play:fake:injoon:안녕");
    }

    @Test
    void 기기_토큰이_없으면_401_과_device_unauthorized() {
        client.post().uri(QuickTtsApi.QUICK_CHAT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new QuickChatRequest("안녕"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.DEVICE_UNAUTHORIZED);
    }

    @Test
    void 연결을_해제한_PC_의_토큰이면_401_과_device_unauthorized() {
        DiscordUserId user = newUser();
        String token = registerDevice(user);
        VOICE_CHANNELS.enter(user, details(newChannel(newGuild()), "게임"));
        manageOwnDevicesUseCase.unlinkAll(new UnlinkAllDevicesCommand(user));

        sendQuickChat(token, "안녕")
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.DEVICE_UNAUTHORIZED);
        assertThat(PLAYER.actions()).isEmpty();
    }

    @Test
    void 빈_문장이나_너무_긴_문장은_400_과_invalid_value() {
        DiscordUserId user = newUser();
        String token = registerDevice(user);
        VOICE_CHANNELS.enter(user, details(newChannel(newGuild()), "게임"));

        sendQuickChat(token, " ")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.INVALID_VALUE);
        sendQuickChat(token, "가".repeat(QuickTtsApi.MAX_TEXT_LENGTH + 1))
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.INVALID_VALUE);
        assertThat(PLAYER.actions()).isEmpty();
    }

    @Test
    void 보낸_사람이_음성_채널에_없으면_409_와_speaker_not_in_voice_channel() {
        String token = registerDevice(newUser());

        sendQuickChat(token, "안녕")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.SPEAKER_NOT_IN_VOICE_CHANNEL);
    }

    @Test
    void 같은_디스코드_서버의_다른_채널에서_쓰이고_있으면_423_과_voice_channel_in_use() {
        GuildId guildId = newGuild();
        VoiceChannel first = newChannel(guildId);
        DiscordUserId firstUser = newUser();
        DiscordUserId secondUser = newUser();
        String firstToken = registerDevice(firstUser);
        String secondToken = registerDevice(secondUser);
        VOICE_CHANNELS.enter(firstUser, details(first, "게임"));
        VOICE_CHANNELS.enter(secondUser, details(newChannel(guildId), "공부"));

        sendQuickChat(firstToken, "먼저").expectStatus().isAccepted();
        sendQuickChat(secondToken, "나중")
                .expectStatus().isEqualTo(423)
                .expectBody()
                .jsonPath("$.code").isEqualTo(ApiErrorCode.VOICE_CHANNEL_IN_USE)
                .jsonPath("$.detail").value(String.class, detail -> assertThat(detail).contains("게임"));

        assertThat(PLAYER.actions()).containsExactly("join:" + first.channelId(), "play:fake:sunhi:먼저");
    }

    @Test
    void TTS_엔진이_음성을_만들지_못하면_502_와_speech_synthesis_failed() {
        DiscordUserId user = newUser();
        String token = registerDevice(user);
        VOICE_CHANNELS.enter(user, details(newChannel(newGuild()), "게임"));
        TTS.failing(true);

        sendQuickChat(token, "안녕")
                .expectStatus().isEqualTo(502)
                .expectBody().jsonPath("$.code").isEqualTo(ApiErrorCode.SPEECH_SYNTHESIS_FAILED);
        assertThat(PLAYER.actions()).isEmpty();
    }

    /** 디스코드 /연결 로 코드를 받아 클라이언트 첫 실행처럼 등록하고 기기 토큰을 돌려준다. */
    private String registerDevice(DiscordUserId owner) {
        String code = issuePairingCodeUseCase.issue(new IssuePairingCodeCommand(owner)).code().display();
        DeviceRegistrationResponse response = postDevice(code)
                .expectStatus().isCreated()
                .expectBody(DeviceRegistrationResponse.class)
                .returnResult().getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.deviceToken()).isNotBlank();
        return response.deviceToken();
    }

    private RestTestClient.ResponseSpec postDevice(String pairingCode) {
        return client.post().uri(QuickTtsApi.DEVICES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DeviceRegistrationRequest(pairingCode, "테스트 PC"))
                .exchange();
    }

    private RestTestClient.ResponseSpec sendQuickChat(String token, String text) {
        return client.post().uri(QuickTtsApi.QUICK_CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, QuickTtsApi.BEARER_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new QuickChatRequest(text))
                .exchange();
    }

    private static DiscordUserId newUser() {
        return new DiscordUserId(IDS.incrementAndGet());
    }

    private static GuildId newGuild() {
        return new GuildId(IDS.incrementAndGet());
    }

    private static VoiceChannel newChannel(GuildId guildId) {
        return new VoiceChannel(guildId, IDS.incrementAndGet());
    }

    private static VoiceChannelDetails details(VoiceChannel channel, String channelName) {
        return new VoiceChannelDetails(channel, "우리 서버", "https://example.com/icon.png", channelName);
    }
}
