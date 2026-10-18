package io.github.phs314.quicktts.bot.device.application.port.in.query;


/**
 * @param rawDeviceToken 요청에 실려 온 기기 토큰 원문. 다른 컨텍스트가 부르므로 device 도메인 타입 대신 원문을 받는다
 */
public record AuthenticateDeviceQuery(String rawDeviceToken) {
}
