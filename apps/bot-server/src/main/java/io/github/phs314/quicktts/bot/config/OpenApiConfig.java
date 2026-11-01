package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.api.ApiErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 웹 API 문서(OpenAPI)의 머리말과 인증 방식. 경로와 요청·응답 모양은 springdoc 이 컨트롤러에서 읽는다.
 * 문서는 {@code /v3/api-docs}, 화면은 {@code /swagger-ui.html} 이다 (ADR 0054).
 */
@Configuration
public class OpenApiConfig {

    /** 기기 토큰을 {@code Authorization: Bearer ...} 로 싣는 인증. 따로 표시한 곳 말고는 모든 API 가 쓴다. */
    public static final String DEVICE_TOKEN = "deviceToken";

    /** 오류 응답(ProblemDetail, RFC 9457) 모양. 컨트롤러의 오류 응답이 {@code #/components/schemas/Problem} 으로 가리킨다. */
    public static final String PROBLEM = "Problem";

    @Bean
    public OpenAPI quickTtsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quick TTS 봇 서버 API")
                        .version("v1")
                        .description("데스크톱 앱이 문장을 보내고, 읽힐 음성 채널을 묻고, 기기를 연결하는 API."))
                .components(new Components()
                        .addSecuritySchemes(DEVICE_TOKEN, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("디스코드 /연결 코드로 기기를 등록하면 받는 기기 토큰"))
                        .addSchemas(PROBLEM, problemSchema()))
                .addSecurityItem(new SecurityRequirement().addList(DEVICE_TOKEN));
    }

    private static Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("오류 응답. 클라이언트는 status 대신 code 로 안내 문구를 고른다 (ADR 0013).")
                .addProperty("type", new StringSchema())
                .addProperty("title", new StringSchema())
                .addProperty("status", new IntegerSchema())
                .addProperty("detail", new StringSchema())
                .addProperty("instance", new StringSchema())
                .addProperty(ApiErrorCode.PROPERTY, new StringSchema()
                        .description("오류 코드 (ApiErrorCode). 예: invalid-value, invalid-device-token"));
    }
}
