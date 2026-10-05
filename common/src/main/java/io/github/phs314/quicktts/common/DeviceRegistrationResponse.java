package io.github.phs314.quicktts.common;

/**
 * @param deviceToken 이 PC 전용 기기 토큰. 클라이언트가 저장해 두고 요청마다 보낸다.
 */
public record DeviceRegistrationResponse(String deviceToken) {
}
