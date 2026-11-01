package io.github.phs314.quicktts.bot.device.application.port.in.query;

/**
 * @param rawDeviceToken 요청 헤더에 실려 온 기기 토큰 원문. 빈 값이나 틀린 모양도 실패로 돌려주려고 값 객체 대신 원문을 받는다
 */
public record AuthenticateDeviceQuery(String rawDeviceToken) {
}
