package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.domain.DeviceToken;
import io.github.phs314.quicktts.bot.device.domain.PairingCode;

/**
 * 연결 코드를 쓰고 그 PC 전용 기기 토큰을 발급한다. 코드는 한 번만 쓸 수 있다.
 */
public interface RegisterDeviceUseCase {

    /**
     * @throws io.github.phs314.quicktts.bot.device.application.InvalidPairingCodeException
     *         없는 코드이거나 만료된 코드일 때
     */
    DeviceToken register(PairingCode code);
}
