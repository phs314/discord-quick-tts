package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.domain.Pairing;
import io.github.phs314.quicktts.bot.device.domain.vo.PairingCode;
import java.time.Duration;

/**
 * 사용자에게 알려 줄 새 연결 코드.
 *
 * @param code     클라이언트에 입력할 연결 코드
 * @param validFor 발급한 때부터 쓸 수 있는 시간
 */
public record IssuedPairingCodeDto(PairingCode code, Duration validFor) {

    public static IssuedPairingCodeDto from(Pairing pairing) {
        return new IssuedPairingCodeDto(pairing.code(), Pairing.VALID_FOR);
    }
}
