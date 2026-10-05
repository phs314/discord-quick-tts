package io.github.phs314.quicktts.common;

/**
 * @param pairingCode 디스코드 {@code /연결} 명령으로 받은 연결 코드
 */
public record DeviceRegistrationRequest(String pairingCode) {
}
