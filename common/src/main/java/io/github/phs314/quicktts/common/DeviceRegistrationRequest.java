package io.github.phs314.quicktts.common;

/**
 * @param pairingCode 디스코드 {@code /연결} 명령으로 받은 연결 코드
 * @param deviceName  {@code /연결해제} 목록에 보여 줄 PC 이름 (보통 컴퓨터 이름)
 */
public record DeviceRegistrationRequest(String pairingCode, String deviceName) {
}
